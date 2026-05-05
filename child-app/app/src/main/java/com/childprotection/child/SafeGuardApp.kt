package com.childprotection.child

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SafeGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val monitoringChannel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                getString(R.string.monitoring_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.monitoring_channel_desc)
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important safety alerts and SOS notifications"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(monitoringChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val MONITORING_CHANNEL_ID = "monitoring_service"
        const val ALERT_CHANNEL_ID = "safety_alerts"
    }
}
