/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dalelalmuslim.knote.data.DatabaseProvider
import com.dalelalmuslim.knote.security.SecurityGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Re-schedules appointment reminders after events that clear or shift pending
 * alarms — device reboot, app update, or a system clock/time-zone change.
 *
 * Scheduled alarms do not survive a reboot, and the app only reschedules them
 * from the UI while it is open. Without this receiver a reminder set before a
 * reboot would silently never fire until the user next opened the app.
 *
 * Rescheduling reads the appointments from the encrypted database, so it can
 * only run when the database is openable without user presence (the no-lock
 * mode). Biometric- and passphrase-protected databases stay encrypted at rest
 * and are rescheduled the next time the user unlocks the app.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> Unit
            else -> return
        }
        val appContext = context.applicationContext
        val pending = goAsync()
        scope.launch {
            try {
                reschedule(appContext)
            } catch (_: Throwable) {
                // Best-effort: a locked or unavailable database simply means the
                // reminders are rescheduled on next unlock instead.
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun reschedule(appContext: Context) {
        val keyManager = SecurityGate.keyManager
        if (!keyManager.isInitialized()) return
        // A locked database (biometric / passphrase) cannot be opened here.
        if (keyManager.requiresUserPresence()) return

        if (!DatabaseProvider.isOpen) {
            DatabaseProvider.open(appContext, keyManager.unlockWithoutPrompt())
        }

        val db = DatabaseProvider.requireDatabase()
        val settings = db.appSettingsDao().getOnce()
        ReminderScheduler.sync(
            context = appContext,
            appointments = db.taskDao().getAllAppointmentsOnce(),
            enabled = settings?.notificationsEnabled ?: true,
            leadMinutes = settings?.notificationLeadMinutes ?: 15,
        )
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
