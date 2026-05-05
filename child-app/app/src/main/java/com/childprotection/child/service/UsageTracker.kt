package com.childprotection.child.service

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

/**
 * Tracks app usage via Android UsageStatsManager API.
 * Only collects data when SCREEN_TIME_TRACKING consent is granted.
 * Sends aggregated signals (app name + duration), NOT raw content.
 */
class UsageTracker(private val context: Context) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * Get total screen time for today in minutes.
     */
    fun getTodayScreenTimeMinutes(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        return stats?.sumOf { it.totalTimeInForeground }?.div(60_000) ?: 0
    }

    /**
     * Get per-app usage for today.
     * Returns aggregated signals only (package name + duration).
     */
    fun getTodayAppUsage(): List<AppUsageData> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        return stats
            ?.filter { it.totalTimeInForeground > 60_000 } // >1 minute
            ?.sortedByDescending { it.totalTimeInForeground }
            ?.take(20) // Top 20 apps only
            ?.map { stat ->
                AppUsageData(
                    packageName = stat.packageName,
                    durationMs = stat.totalTimeInForeground,
                    durationMinutes = stat.totalTimeInForeground / 60_000,
                    lastUsed = stat.lastTimeUsed
                )
            } ?: emptyList()
    }

    /**
     * Get app name from package name safely.
     */
    fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".")
        }
    }
}

data class AppUsageData(
    val packageName: String,
    val durationMs: Long,
    val durationMinutes: Long,
    val lastUsed: Long
)
