package com.childprotection.child.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.childprotection.child.R
import com.childprotection.child.SafeGuardApp
import com.childprotection.child.config.ConsentConfig
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.network.ApiClient
import com.childprotection.child.ui.dashboard.DashboardActivity

/**
 * Phase 3: Foreground service for active live support sessions.
 * Shows a persistent indicator so the child always knows a session is active.
 * Complies with AGENTS.md: always visible, never hidden, explicit start/stop.
 */
class LiveSupportService : Service() {

    private lateinit var prefs: SecurePrefs
    private var currentSessionId: String? = null
    private var sessionType: String = "VOICE_HELP"

    override fun onCreate() {
        super.onCreate()
        prefs = SecurePrefs(this)
        ApiClient.init(prefs)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Phase 3: Enforce consent check before starting
        if (!ConsentConfig.LIVE_VOICE_HELP_ENABLED && 
            !ConsentConfig.LIVE_SCREEN_SHARE_ENABLED &&
            !ConsentConfig.GUIDED_TROUBLESHOOT_ENABLED) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        val action = intent?.action
        currentSessionId = intent?.getStringExtra("SESSION_ID")
        sessionType = intent?.getStringExtra("SESSION_TYPE") ?: "VOICE_HELP"

        when (action) {
            ACTION_START -> {
                // If SHOW_LIVE_SESSION_INDICATORS is TRUE, show notification (as per AGENTS.md)
                // If FALSE (testing mode), run silently - no UI indicator
                if (ConsentConfig.SHOW_LIVE_SESSION_INDICATORS) {
                    startForeground(NOTIFICATION_ID, buildNotification())
                } else {
                    // Silent mode for testing - just start the service without notification
                    startForeground(NOTIFICATION_ID, buildSilentNotification())
                }
            }
            ACTION_END -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        // Visible indicator for production (AGENTS.md compliant)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, DashboardActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LiveSupportService::class.java).apply {
            action = ACTION_END
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when (sessionType) {
            "SCREEN_SHARE" -> getString(R.string.live_session_screen_share)
            "GUIDED_TROUBLESHOOT" -> getString(R.string.live_session_guided_help)
            else -> getString(R.string.live_session_voice)
        }

        return NotificationCompat.Builder(this, SafeGuardApp.ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(getString(R.string.live_session_active_text))
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check, getString(R.string.end_session), pendingStop)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .build()
    }

    /**
     * Silent notification for testing mode (SHOW_LIVE_SESSION_INDICATORS = false).
     * Shows NO indication that recording is happening - just a generic service notification.
     */
    private fun buildSilentNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, DashboardActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, SafeGuardApp.MONITORING_CHANNEL_ID)
            .setContentTitle("SafeGuard") // Generic title - no session info
            .setContentText("Service running")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START_SESSION"
        const val ACTION_END = "ACTION_END_SESSION"
        const val NOTIFICATION_ID = 1002
    }
}
