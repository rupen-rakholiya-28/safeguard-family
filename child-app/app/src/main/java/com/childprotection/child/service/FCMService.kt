package com.childprotection.child.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

/**
 * Firebase Cloud Messaging service for receiving push notifications
 * from parents (policy updates, messages, etc.)
 */
class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "Message received: ${message.data}")
        // Handle push notifications (policy sync, parent messages)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New FCM token: $token")
        // Send token to backend for push notification targeting
    }
}
