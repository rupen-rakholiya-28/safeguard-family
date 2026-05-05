package com.childprotection.child.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.childprotection.child.R
import com.childprotection.child.SafeGuardApp
import com.childprotection.child.config.ConsentConfig
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.network.ApiClient
import com.childprotection.child.ui.dashboard.DashboardActivity
import kotlinx.coroutines.*

/**
 * Foreground service for transparent monitoring.
 * 
 * TRANSPARENCY: Shows a persistent notification so the child always
 * knows monitoring is active. This is NOT hidden or stealth —
 * it's a core design principle per AGENTS.md compliance.
 * 
 * The service:
 * - Sends periodic heartbeats to the backend
 * - Syncs usage data at regular intervals
 * - Reports location (if consent granted)
 * - Shows persistent "monitoring active" notification
 * 
 * All activity controlled by ConsentConfig - if a feature is disabled,
 * this service won't track it.
 */
class MonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: SecurePrefs
    private lateinit var riskDetectionService: RiskDetectionService
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                serviceScope.launch { sendLocation(location) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = SecurePrefs(this)
        ApiClient.init(prefs)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Initialize risk detection only if enabled in ConsentConfig
        if (ConsentConfig.RISK_DETECTION_ENABLED) {
            riskDetectionService = RiskDetectionService(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startPeriodicSync()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, DashboardActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, SafeGuardApp.MONITORING_CHANNEL_ID)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(getString(R.string.monitoring_notification_text))
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startPeriodicSync() {
        // Start location updates if enabled
        if (ConsentConfig.LOCATION_SHARING_ENABLED && hasLocationPermission()) {
            startLocationUpdates()
        }
        
        // Heartbeat every 5 minutes
        serviceScope.launch {
            while (isActive) {
                sendHeartbeat()
                delay(5 * 60 * 1000L) // 5 minutes
            }
        }

        // Usage sync every 15 minutes - only if enabled
        if (ConsentConfig.SCREEN_TIME_TRACKING_ENABLED || ConsentConfig.APP_USAGE_TRACKING_ENABLED) {
            serviceScope.launch {
                while (isActive) {
                    delay(15 * 60 * 1000L) // 15 minutes
                    syncUsageData()
                }
            }
        }
        
        // Risk detection check every hour - only if enabled
        if (ConsentConfig.RISK_DETECTION_ENABLED) {
            serviceScope.launch {
                while (isActive) {
                    delay(60 * 60 * 1000L) // 1 hour
                    try {
                        riskDetectionService.runDetections()
                    } catch (e: Exception) {
                        // Silent fail - don't crash the main service
                    }
                }
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5 * 60 * 1000L)
            .setMinUpdateIntervalMillis(3 * 60 * 1000L)
            .build()
            
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
    
    private suspend fun sendLocation(location: Location) {
        // Check if user granted location consent
        if (!prefs.isConsentGranted("LOCATION_SHARING")) {
            return
        }
        
        try {
            val body = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "timestamp" to System.currentTimeMillis()
            )
            ApiClient.getService().reportLocation(body)
        } catch (e: Exception) {
            // Will retry next cycle
        }
    }

    private suspend fun sendHeartbeat() {
        try {
            val deviceId = prefs.deviceId ?: return
            val batteryLevel = getBatteryLevel()
            ApiClient.getService().sendHeartbeat(
                deviceId,
                mapOf("batteryLevel" to batteryLevel)
            )
        } catch (e: Exception) {
            // Will retry next cycle
        }
    }

    private suspend fun syncUsageData() {
        // Check if we have consent for screen time or app usage
        if (!ConsentConfig.SCREEN_TIME_TRACKING_ENABLED && 
            !ConsentConfig.APP_USAGE_TRACKING_ENABLED) {
            return
        }
        
        // Also check if user granted consent in settings
        if (!prefs.isConsentGranted("SCREEN_TIME_TRACKING") && 
            !prefs.isConsentGranted("APP_USAGE_TRACKING")) {
            return
        }
        
        try {
            val tracker = UsageTracker(this@MonitoringService)
            val appUsage = tracker.getTodayAppUsage()

            if (appUsage.isEmpty()) return

            val events = appUsage.map { usage ->
                mapOf(
                    "packageName" to usage.packageName,
                    "appName" to tracker.getAppName(usage.packageName),
                    "durationMinutes" to usage.durationMinutes,
                    "date" to java.time.LocalDate.now().toString()
                )
            }

            val body = mapOf<String, Any>(
                "deviceId" to (prefs.deviceId ?: ""),
                "events" to events
            )

            ApiClient.getService().reportUsageEvents(body)
        } catch (e: Exception) {
            // Will retry next cycle
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}