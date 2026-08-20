/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import com.dalelalmuslim.knote.R
import com.dalelalmuslim.knote.data.Task
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {

    const val CHANNEL_ID = "appointment_reminders"

    const val ACTION_REMIND = "com.dalelalmuslim.knote.action.APPOINTMENT_REMINDER"
    const val EXTRA_TITLE   = "extra_title"
    const val EXTRA_TEXT    = "extra_text"
    const val EXTRA_ID      = "extra_id"

    private const val PREFS   = "appointment_reminders"
    private const val KEY_IDS = "scheduled_ids"

    fun sync(
        context: Context,
        appointments: List<Task>,
        enabled: Boolean,
        leadMinutes: Int,
    ) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty()

        // 1. Schedule all currently-valid alarms first. Re-using the same request
        //    code replaces any existing alarm in place, so a still-valid reminder is
        //    never left unscheduled — there is no window where it can be lost.
        val scheduled = mutableSetOf<String>()
        if (enabled) {
            val now = System.currentTimeMillis()
            appointments.forEach { task ->
                if (!task.isAppointment || task.time.isBlank() || task.isDone) return@forEach
                val triggerAt = (appointmentMillis(task) ?: return@forEach) - leadMinutes * 60_000L
                if (triggerAt <= now) return@forEach
                val text = reminderText(context, task.time, leadMinutes)
                schedule(am, context, task.id, task.title, text, triggerAt)
                scheduled += task.id.toString()
            }
        }

        // 2. Cancel only the alarms that are no longer needed (previously scheduled
        //    but not in the current set), then persist the new set.
        (previous - scheduled).forEach { idStr ->
            idStr.toLongOrNull()?.let { id ->
                am.cancel(buildPendingIntent(context, id, null, null))
            }
        }
        prefs.edit { putStringSet(KEY_IDS, scheduled) }
    }

    // Lint asks for SCHEDULE_EXACT_ALARM, but the app declares the always-granted
    // USE_EXACT_ALARM instead, and the call is guarded by canScheduleExactAlarms()
    // either way. Lint does not recognise that combination.
    @SuppressLint("MissingPermission")
    private fun schedule(
        am: AlarmManager,
        context: Context,
        id: Long,
        title: String,
        text: String,
        triggerAt: Long,
    ) {
        val pi = buildPendingIntent(context, id, title, text)
        // The app holds USE_EXACT_ALARM (see AndroidManifest), so canScheduleExactAlarms()
        // is always true here and reminders fire at the exact minute. The inexact branch is
        // kept purely as a defensive fallback and is not reached in normal operation.
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            // Exact-alarm scheduling permission didn't exist before API 31;
            // it was always implicitly allowed.
            true
        }
        if (canScheduleExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun buildPendingIntent(
        context: Context,
        id: Long,
        title: String?,
        text: String?,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra(EXTRA_ID, id)
            if (title != null) putExtra(EXTRA_TITLE, title)
            if (text != null) putExtra(EXTRA_TEXT, text)
        }
        var flags = PendingIntent.FLAG_IMMUTABLE
        flags = flags or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, id.toInt(), intent, flags)
    }

    private fun appointmentMillis(task: Task): Long? {
        val parts = task.time.split(":")
        val hour   = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalDate.ofEpochDay(task.date)
            .atTime(LocalTime.of(hour, minute))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun reminderText(context: Context, time: String, leadMinutes: Int): String = when {
        leadMinutes <= 0 -> context.getString(R.string.reminder_now, time)
        leadMinutes % 60 == 0 -> {
            val h = leadMinutes / 60
            context.resources.getQuantityString(R.plurals.reminder_in_hours, h, h, time)
        }
        else -> context.resources.getQuantityString(R.plurals.reminder_in_minutes, leadMinutes, leadMinutes, time)
    }
}
