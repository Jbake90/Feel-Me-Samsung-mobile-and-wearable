package com.feelme.wear

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class WearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Feel Me",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Friend pings"
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "feelme_wear"
    }
}
