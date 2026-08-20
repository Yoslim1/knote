/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.dalelalmuslim.knote.notification.ReminderScheduler
import com.dalelalmuslim.knote.security.KeyManager
import com.dalelalmuslim.knote.security.SecurityGate

class KnoteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        SecurityGate.install(KeyManager(this))
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            getString(R.string.notif_channel_appointments),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notif_channel_appointments_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
