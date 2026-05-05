package com.childprotection.parent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SafeGuardParentApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Receive safety alerts and SOS notifications from your children"
            }

            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val ALERT_CHANNEL_ID = "safety_alerts"
        const val GENERAL_CHANNEL_ID = "general"
    }
}
