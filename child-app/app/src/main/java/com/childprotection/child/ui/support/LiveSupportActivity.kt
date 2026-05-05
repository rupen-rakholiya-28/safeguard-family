package com.childprotection.child.ui.support

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.childprotection.child.R
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.databinding.ActivityLiveSupportBinding
import com.childprotection.child.network.ApiClient
import com.childprotection.child.service.LiveSupportService
import kotlinx.coroutines.launch

class LiveSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveSupportBinding
    private lateinit var prefs: SecurePrefs
    private var currentSessionId: String? = null
    private val AUDIO_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SecurePrefs(this)
        ApiClient.init(prefs)

        setupUI()
    }

    private fun setupUI() {
        binding.btnStartSession.setOnClickListener {
            // Skip consent dialog during testing (DEBUG mode)
            if (!prefs.isConsentGranted("LIVE_SUPPORT") && !com.childprotection.child.BuildConfig.DEBUG) {
                showConsentDialog()
                return@setOnClickListener
            }
            startSession()
        }

        binding.btnEndSession.setOnClickListener {
            endSession()
        }
    }

    private fun showConsentDialog() {
        AlertDialog.Builder(this)
            .setTitle("Live Support Consent")
            .setMessage("This feature allows you to start a live help session with your parent. " +
                    "The session will be visible and logged. You can end it anytime. " +
                    "Do you agree to enable live support?")
            .setPositiveButton("Allow") { _, _ ->
                prefs.setConsentGranted("LIVE_SUPPORT", true)
                Toast.makeText(this, "Consent granted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun startSession() {
        val type = when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radioVoice -> getString(R.string.type_voice)
            R.id.radioScreen -> getString(R.string.type_screen)
            else -> "GUIDED_TROUBLESHOOT"
        }

        if (type == getString(R.string.type_voice)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    AUDIO_PERMISSION_CODE
                )
                return
            }
        }

        createSession(type)
    }

    private fun createSession(type: String) {
        lifecycleScope.launch {
            try {
                val body = mapOf("childId" to (prefs.childId ?: ""), "type" to type)
                val response = ApiClient.getService().createSupportSession(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentSessionId = response.body()?.data?.id
                    binding.tvStatus.text = "Session Active ($type)"
                    binding.btnStartSession.isEnabled = false
                    binding.btnEndSession.isEnabled = true
                    updateLogs(response.body()?.data?.logs)

                    // Start foreground service for visible indicator
                    val intent = Intent(this@LiveSupportActivity, LiveSupportService::class.java).apply {
                        action = LiveSupportService.ACTION_START
                        putExtra("SESSION_ID", currentSessionId)
                        putExtra("SESSION_TYPE", type)
                    }
                    startForegroundService(intent)

                    Toast.makeText(this@LiveSupportActivity, getString(R.string.session_started), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LiveSupportActivity, getString(R.string.session_error), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LiveSupportActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun endSession() {
        lifecycleScope.launch {
            try {
                val sessionId = currentSessionId ?: return@launch
                val response = ApiClient.getService().endSupportSession(sessionId)
                if (response.isSuccessful) {
                    binding.tvStatus.text = "Session Ended"
                    binding.btnStartSession.isEnabled = true
                    binding.btnEndSession.isEnabled = false
                    updateLogs(response.body()?.data?.logs)
                    currentSessionId = null

                    // Stop foreground service
                    val intent = Intent(this@LiveSupportActivity, LiveSupportService::class.java).apply {
                        action = LiveSupportService.ACTION_END
                    }
                    startService(intent)

                    Toast.makeText(this@LiveSupportActivity, getString(R.string.session_ended), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LiveSupportActivity, "Error ending session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLogs(logs: String?) {
        binding.tvLogs.text = logs ?: "No logs yet"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val type = getString(R.string.type_voice)
                createSession(type)
            } else {
                Toast.makeText(this, "Microphone permission required for voice help", Toast.LENGTH_LONG).show()
            }
        }
    }
}
