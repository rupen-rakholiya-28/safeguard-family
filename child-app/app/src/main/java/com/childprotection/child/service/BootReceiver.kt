package com.childprotection.child.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Boot receiver to restart monitoring service after device reboot.
 * Only starts if the child is onboarded and has active consents.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = com.childprotection.child.data.SecurePrefs(context)
            if (prefs.isOnboarded && prefs.isLoggedIn) {
                val serviceIntent = Intent(context, MonitoringService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
