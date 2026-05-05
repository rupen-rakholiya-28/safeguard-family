package com.childprotection.child.ui.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.databinding.ActivityOnboardingBinding
import com.childprotection.child.network.ApiClient
import com.childprotection.child.ui.consent.ConsentActivity
import com.childprotection.child.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

/**
 * Onboarding screen: Child joins a family using an invite code.
 * Transparent flow - child clearly sees what family they're joining.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = SecurePrefs(this)
        ApiClient.init(prefs)

        // If already onboarded, go to dashboard
        if (prefs.isOnboarded && prefs.isLoggedIn) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        // If logged in but not onboarded (consent pending), go to consent
        if (prefs.isLoggedIn && !prefs.isOnboarded) {
            startActivity(Intent(this, ConsentActivity::class.java))
            finish()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnJoinFamily.setOnClickListener {
            val inviteCode = binding.etInviteCode.text.toString().trim().uppercase()
            val name = binding.etChildName.text.toString().trim()
            val email = binding.etChildEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (inviteCode.isEmpty() || name.isEmpty() || email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Please fill all fields (password min 6 chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            joinFamily(inviteCode, name, email, password)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etChildEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signIn(email, password)
        }
    }

    private fun joinFamily(inviteCode: String, name: String, email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getService().joinFamily(
                    mapOf(
                        "inviteCode" to inviteCode,
                        "displayName" to name,
                        "email" to email,
                        "password" to password
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    prefs.childId = data.childId
                    prefs.familyId = data.familyId
                    prefs.familyName = data.familyName
                    prefs.childName = data.childName

                    // Now login to get tokens
                    val loginRes = ApiClient.getService().login(
                        mapOf("email" to email, "password" to password)
                    )
                    if (loginRes.isSuccessful && loginRes.body()?.success == true) {
                        val loginData = loginRes.body()!!.data!!
                        prefs.accessToken = loginData.accessToken
                        prefs.refreshToken = loginData.refreshToken

                        // Register device
                        registerDevice()

                        // Go to consent walkthrough
                        startActivity(Intent(this@OnboardingActivity, ConsentActivity::class.java))
                        finish()
                    }
                } else {
                    val msg = response.body()?.message ?: "Failed to join family"
                    Toast.makeText(this@OnboardingActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@OnboardingActivity,
                    "Connection error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun signIn(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getService().login(
                    mapOf("email" to email, "password" to password)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    prefs.accessToken = data.accessToken
                    prefs.refreshToken = data.refreshToken
                    prefs.childId = data.user.id
                    prefs.childName = data.user.displayName

                    if (prefs.isOnboarded) {
                        startActivity(Intent(this@OnboardingActivity, DashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this@OnboardingActivity, ConsentActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this@OnboardingActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "Connection error", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun registerDevice() {
        try {
            val response = ApiClient.getService().registerDevice(
                mapOf(
                    "deviceName" to Build.DEVICE,
                    "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
                    "osVersion" to "Android ${Build.VERSION.RELEASE}",
                    "appVersion" to "1.0.0"
                )
            )
            if (response.isSuccessful) {
                prefs.deviceId = response.body()?.data?.id
            }
        } catch (e: Exception) {
            // Device registration will retry later
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnJoinFamily.isEnabled = !loading
        binding.btnSignIn.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
