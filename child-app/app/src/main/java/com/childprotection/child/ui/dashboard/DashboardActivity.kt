package com.childprotection.child.ui.dashboard

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AppOpsManagerCompat
import androidx.lifecycle.lifecycleScope
import com.childprotection.child.R
import com.childprotection.child.config.ConsentConfig
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.databinding.ActivityDashboardBinding
import com.childprotection.child.network.ApiClient
import com.childprotection.child.service.MonitoringService
import com.childprotection.child.service.UsageTracker
import com.childprotection.child.ui.onboarding.OnboardingActivity
import com.childprotection.child.ui.support.LiveSupportActivity
import kotlinx.coroutines.launch

/**
 * Child dashboard showing:
 * - What's currently being monitored (transparency)
 * - Screen time today
 * - SOS button
 * - Disconnect request option
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SecurePrefs(this)
        ApiClient.init(prefs)

        if (!prefs.isLoggedIn) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setupUI()
        checkUsagePermission()
        startMonitoringService()
    }

    override fun onResume() {
        super.onResume()
        updateScreenTime()
        updateConsentStatus()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Hi, ${prefs.childName ?: "there"} 👋"
        binding.tvFamilyName.text = prefs.familyName ?: "Your Family"

        // Quick Actions - Location
        binding.cardLocation.setOnClickListener {
            showLocationInfo()
        }

        // Quick Actions - Usage
        binding.cardUsage.setOnClickListener {
            showUsageInfo()
        }

        // SOS Button (Phase 3 - Emergency)
        binding.btnSOS.isEnabled = ConsentConfig.SOS_ENABLED
        binding.btnSOS.setOnClickListener {
            showSOSConfirmation()
        }

        // Live Support Button (Phase 3 - Guided Assistance)
        binding.btnLiveSupport.isEnabled = ConsentConfig.LIVE_VOICE_HELP_ENABLED || 
                                    ConsentConfig.LIVE_SCREEN_SHARE_ENABLED
        binding.btnLiveSupport.setOnClickListener {
            startActivity(Intent(this, LiveSupportActivity::class.java))
        }

        // Transparency: What's monitored
        binding.btnViewMonitoring.setOnClickListener {
            showMonitoringDetails()
        }

        // Disconnect request
        binding.btnDisconnect.setOnClickListener {
            showDisconnectDialog()
        }

        // Phase 1 features - show/hide based on consent config
        // Only show screen time if enabled
        if (!ConsentConfig.SCREEN_TIME_TRACKING_ENABLED) {
            binding.tvScreenTimeLabel.text = "Screen Time (Disabled)"
            binding.tvScreenTime.text = "--"
        }
    }

    private fun showLocationInfo() {
        AlertDialog.Builder(this)
            .setTitle("📍 Location Sharing")
            .setMessage("Your location is shared with your parent when enabled. " +
                    "This helps keep you safe and allows parents to see your whereabouts.\n\n" +
                    "Location is only shared when you give explicit consent.")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showUsageInfo() {
        AlertDialog.Builder(this)
            .setTitle("📱 App Usage")
            .setMessage("Your screen time and app usage are tracked to help parents understand your digital habits. " +
                    "This data helps set healthy screen time limits.\n\n" +
                    "You can see your daily screen time on this dashboard.")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun updateScreenTime() {
        if (hasUsagePermission()) {
            val tracker = UsageTracker(this)
            val totalMinutes = tracker.getTodayScreenTimeMinutes()
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            binding.tvScreenTime.text = "${hours}h ${mins}m"
            binding.tvScreenTimeLabel.text = getString(R.string.screen_time_today)
        } else {
            binding.tvScreenTime.text = "--"
            binding.tvScreenTimeLabel.text = "Usage access not granted"
        }
    }

    private fun updateConsentStatus() {
        val features = listOf(
            "SCREEN_TIME_TRACKING" to "Screen Time",
            "APP_USAGE_TRACKING" to "App Usage",
            "LOCATION_SHARING" to "Location",
            "EMERGENCY_CONTACT_SHARING" to "Emergency"
        )

        val activeFeatures = features.filter { prefs.isConsentGranted(it.first) }
        binding.tvActiveFeatures.text = if (activeFeatures.isEmpty()) {
            "No features active"
        } else {
            activeFeatures.joinToString(" • ") { it.second }
        }

        binding.tvMonitoringStatus.text = if (activeFeatures.isNotEmpty()) {
            getString(R.string.monitoring_active)
        } else {
            getString(R.string.monitoring_paused)
        }
    }

    private fun showSOSConfirmation() {
        val helplines = arrayOf(
            "Call Emergency Services (911)",
            "Child Helpline (1098)",
            "Mental Health Support"
        )

        val options = arrayOf("Send SOS Alert", *helplines, "Cancel")

        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency Help")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendSOSAlert()
                    1 -> callNumber("911")
                    2 -> callNumber("1098")
                    3 -> callHelpline()
                    4 -> {} // Cancel
                }
            }
            .show()
    }

    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    private fun callHelpline() {
        val url = "https://www.childhelplineinternational.org/find-a-helpline/"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        startActivity(intent)
    }

    private fun sendSOSAlert() {
        lifecycleScope.launch {
            try {
                val body = mutableMapOf(
                    "childId" to (prefs.childId ?: ""),
                    "alertType" to "SOS",
                    "severity" to "CRITICAL",
                    "title" to "SOS Alert from ${prefs.childName}",
                    "message" to "${prefs.childName} pressed the SOS button and needs help!"
                )
                prefs.deviceId?.let { body["deviceId"] = it }

                // Phase 3: Share location during emergency only
                if (prefs.isConsentGranted("LOCATION_SHARING")) {
                    body["emergencyLocation"] = "true"
                    body["message"] = "${body["message"]} Location will be shared."
                }

                val response = ApiClient.getService().createAlert(body)
                if (response.isSuccessful) {
                    Toast.makeText(this@DashboardActivity, getString(R.string.sos_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to send SOS", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "No connection — try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMonitoringDetails() {
        // Phase 3 & 7 Transparency Updates
        val features = listOf(
            "SCREEN_TIME_TRACKING" to "⏱️ Screen Time Tracking",
            "APP_USAGE_TRACKING" to "📱 App Usage Tracking",
            "LOCATION_SHARING" to "📍 Location Sharing",
            "WEB_PROTECTION" to "🌐 Web Filtering",
            "RISK_DETECTION" to "🛡️ Safety Scanning",
            "LIVE_SUPPORT" to "🎙️ Live Assistance",
            "EMERGENCY_CONTACT_SHARING" to "🆘 Emergency Contacts"
        )

        val message = buildString {
            appendLine("These features are currently active:\n")
            for ((key, label) in features) {
                val status = if (prefs.isConsentGranted(key)) "✅ ON" else "❌ OFF"
                appendLine("$label — $status")
            }
            appendLine("\nYou or your parent can turn any feature off at any time.")
        }

        AlertDialog.Builder(this)
            .setTitle("🔍 What's Being Monitored")
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun showDisconnectDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disconnect Request")
            .setMessage("This will send a disconnect request to your parent. Monitoring will stop after parent approves.")
            .setPositiveButton("Request") { _, _ ->
                sendDisconnectRequest()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun sendDisconnectRequest() {
        lifecycleScope.launch {
            try {
                val body = mutableMapOf(
                    "childId" to (prefs.childId ?: ""),
                    "alertType" to "CUSTOM",
                    "severity" to "HIGH",
                    "title" to "Device Disconnect Request",
                    "message" to "${prefs.childName} has requested to disconnect their device."
                )
                prefs.deviceId?.let { body["deviceId"] = it }

                val response = ApiClient.getService().createAlert(body)
                if (response.isSuccessful) {
                    Toast.makeText(this@DashboardActivity, "Disconnect request sent to parent", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "No connection — try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUsagePermission() {
        if (!hasUsagePermission() && prefs.isConsentGranted("SCREEN_TIME_TRACKING")) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_usage_title))
                .setMessage(getString(R.string.permission_usage_desc))
                .setPositiveButton(getString(R.string.grant)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun hasUsagePermission(): Boolean {
        val mode = AppOpsManagerCompat.noteOpNoThrow(
            this,
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManagerCompat.MODE_ALLOWED
    }

    private fun startMonitoringService() {
        val intent = Intent(this, MonitoringService::class.java)
        startForegroundService(intent)
    }
}
