package com.childprotection.parent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.ActivityAuthBinding
import com.childprotection.parent.network.ApiClient
import com.childprotection.parent.ui.dashboard.MainActivity
import kotlinx.coroutines.launch

/**
 * Auth screen with login/signup toggle.
 * AGENTS.md: Validate all inputs, clear error messages.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var prefs: ParentPrefs
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = ParentPrefs(this)
        ApiClient.init(prefs)

        if (prefs.isLoggedIn) {
            goToDashboard()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        updateMode()

        binding.btnSubmit.setOnClickListener {
            if (isLoginMode) performLogin() else performSignup()
        }

        binding.btnSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateMode()
        }
    }

    private fun updateMode() {
        binding.tvTitle.text = if (isLoginMode) getString(com.childprotection.parent.R.string.login_title) else getString(com.childprotection.parent.R.string.signup_title)
        binding.tvSubtitle.text = if (isLoginMode) getString(com.childprotection.parent.R.string.login_subtitle) else getString(com.childprotection.parent.R.string.signup_subtitle)
        binding.tilName.visibility = if (isLoginMode) View.GONE else View.VISIBLE
        binding.btnSubmit.text = if (isLoginMode) getString(com.childprotection.parent.R.string.sign_in) else getString(com.childprotection.parent.R.string.sign_up)
        binding.btnSwitchMode.text = if (isLoginMode) getString(com.childprotection.parent.R.string.no_account) else getString(com.childprotection.parent.R.string.has_account)
        binding.tvError.visibility = View.GONE
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService().login(
                    mapOf("email" to email, "password" to password)
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    val data = res.body()!!.data!!
                    saveAuth(data)
                    goToDashboard()
                } else {
                    showError(res.body()?.message ?: "Invalid credentials")
                }
            } catch (e: Exception) {
                showError("Connection error: ${e.localizedMessage}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun performSignup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.length < 6) {
            showError("Fill all fields (password min 6 chars)")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService().signup(
                    mapOf("displayName" to name, "email" to email, "password" to password)
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    val data = res.body()!!.data!!
                    saveAuth(data)
                    goToDashboard()
                } else {
                    showError(res.body()?.message ?: "Signup failed")
                }
            } catch (e: Exception) {
                showError("Connection error: ${e.localizedMessage}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun saveAuth(data: com.childprotection.parent.network.AuthData) {
        prefs.accessToken = data.accessToken
        prefs.refreshToken = data.refreshToken
        prefs.userId = data.user.id
        prefs.displayName = data.user.displayName
        prefs.email = data.user.email
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSubmit.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
