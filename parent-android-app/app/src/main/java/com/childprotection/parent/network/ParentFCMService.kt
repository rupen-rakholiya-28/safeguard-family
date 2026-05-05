package com.childprotection.parent.network

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.childprotection.parent.R
import com.childprotection.parent.SafeGuardParentApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM service for receiving push notifications:
 * - SOS alerts from children
 * - Policy sync confirmations
 * - Child consent changes
 */
class ParentFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("ParentFCM", "Message: ${message.data}")

        val title = message.data["title"] ?: message.notification?.title ?: "SafeGuard Alert"
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val type = message.data["type"] ?: "GENERAL"

        val channelId = if (type == "SOS" || type == "CRITICAL")
            SafeGuardParentApp.ALERT_CHANNEL_ID
        else
            SafeGuardParentApp.GENERAL_CHANNEL_ID

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_shield)
            .setAutoCancel(true)
            .setPriority(
                if (type == "SOS") NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("ParentFCM", "New token: $token")
    }
}
