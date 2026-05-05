package com.childprotection.child.service

import android.content.Context
import com.childprotection.child.network.ApiClient
import com.childprotection.child.data.SecurePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

/**
 * Simple on‑device risk detection for Phase 2.
 *
 * This is a placeholder implementation that uses usage signals to generate
 * risk events and reports them to the backend via `ApiService.reportRiskEvent`.
 * It respects consent – it only runs when SCREEN_TIME_TRACKING consent is granted.
 * All detections are sent with source = "ON_DEVICE" and a high confidence score.
 */
class RiskDetectionService(context: Context) {

    private val prefs = SecurePrefs(context)
    private val tracker = UsageTracker(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Start the periodic detection loop.
     * Runs every 15 minutes (configurable) while the service is active.
     */
    fun start() {
        scope.launch {
            while (isActive) {
                try {
                    runDetections()
                } catch (e: Exception) {
                    // Swallow – next cycle will retry
                }
                delay(15 * 60 * 1000L) // 15 minutes
            }
        }
    }

    internal suspend fun runDetections() {
        if (!prefs.isConsentGranted("SCREEN_TIME_TRACKING")) return

        // 1️⃣ Excessive screen time (e.g., >180 min)
        val totalMinutes = tracker.getTodayScreenTimeMinutes()
        if (totalMinutes > 180) {
            reportRisk(
                category = "EXCESSIVE_SCREEN_TIME",
                level = "HIGH",
                confidence = 0.95,
                title = "High screen time detected",
                description = "Child has used the device for $totalMinutes minutes today.",
                relatedApp = null
            )
        }

        // 2️⃣ Late‑night usage – any app used after 22:00
        val now = LocalTime.now(ZoneId.systemDefault())
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
            // Check if any recent usage entry falls in this window
            val recentApps = tracker.getTodayAppUsage().filter { it.lastUsed > System.currentTimeMillis() - 30 * 60 * 1000 }
            if (recentApps.isNotEmpty()) {
                reportRisk(
                    category = "LATE_NIGHT_USAGE",
                    level = "MEDIUM",
                    confidence = 0.85,
                    title = "Late‑night device usage",
                    description = "Device was used during night hours (${now}).",
                    relatedApp = recentApps.firstOrNull()?.packageName
                )
            }
        }

        // 3️⃣ New risky app detection – any app not previously known
        val knownApps = prefs.getKnownApps()
        val todayApps = tracker.getTodayAppUsage().map { it.packageName }
        val newApps = todayApps.filter { it !in knownApps }
        if (newApps.isNotEmpty()) {
            // Report the first new app as a potential risk (could be expanded)
            val pkg = newApps.first()
            reportRisk(
                category = "NEW_RISKY_APP",
                level = "MEDIUM",
                confidence = 0.8,
                title = "New app usage detected",
                description = "App $pkg was used for the first time today.",
                relatedApp = pkg
            )
            // Persist the discovered apps for future runs
            prefs.addKnownApps(setOf(pkg))
        }
    }

    private suspend fun reportRisk(
        category: String,
        level: String,
        confidence: Double,
        title: String,
        description: String,
        relatedApp: String?
    ) {
        val body = mutableMapOf<String, Any?>(
            "riskCategory" to category,
            "riskLevel" to level,
            "confidence" to confidence,
            "title" to title,
            "description" to description,
            "source" to "ON_DEVICE"
        )
        relatedApp?.let { body["relatedAppPackage"] = it }
        // Optional deviceId for correlation
        prefs.deviceId?.let { body["deviceId"] = it }
        ApiClient.getService().reportRiskEvent(body)
    }
}
