package com.sokcuri.twimgspeedproxy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


class AppNotification : Application() {
    private fun CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "App Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        val CHANNEL_ID = "AppNotificationChannel"
    }
}