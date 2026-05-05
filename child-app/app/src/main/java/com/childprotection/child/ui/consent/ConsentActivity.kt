package com.childprotection.child.ui.consent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.childprotection.child.R
import com.childprotection.child.data.SecurePrefs
import com.childprotection.child.databinding.ActivityConsentBinding
import com.childprotection.child.network.ApiClient
import com.childprotection.child.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

/**
 * Consent walkthrough screen.
 * Shows each trackable feature with clear description of what it does,
 * why it's needed, and what it does NOT do.
 * Child must explicitly approve each feature — consent-first approach.
 */
class ConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsentBinding
    private lateinit var prefs: SecurePrefs
    private val consentItems = mutableListOf<ConsentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SecurePrefs(this)

        setupConsentItems()
        setupRecyclerView()

        binding.btnFinishSetup.setOnClickListener {
            saveConsents()
        }
    }

    private fun setupConsentItems() {
        consentItems.addAll(listOf(
            ConsentItem(
                feature = "SCREEN_TIME_TRACKING",
                title = getString(R.string.consent_screen_time),
                description = getString(R.string.consent_screen_time_desc),
                why = getString(R.string.consent_screen_time_why),
                whatNot = getString(R.string.consent_screen_time_not),
                icon = R.drawable.ic_screen_time
            ),
            ConsentItem(
                feature = "APP_USAGE_TRACKING",
                title = getString(R.string.consent_app_usage),
                description = getString(R.string.consent_app_usage_desc),
                why = getString(R.string.consent_app_usage_why),
                whatNot = getString(R.string.consent_app_usage_not),
                icon = R.drawable.ic_app_usage
            ),
            ConsentItem(
                feature = "LOCATION_SHARING",
                title = getString(R.string.consent_location),
                description = getString(R.string.consent_location_desc),
                why = getString(R.string.consent_location_why),
                whatNot = getString(R.string.consent_location_not),
                icon = R.drawable.ic_location
            ),
            ConsentItem(
                feature = "EMERGENCY_CONTACT_SHARING",
                title = getString(R.string.consent_emergency),
                description = getString(R.string.consent_emergency_desc),
                why = "Ensures help is always available",
                whatNot = "Does NOT auto-dial without your action",
                icon = R.drawable.ic_emergency
            )
        ))
    }

    private fun setupRecyclerView() {
        binding.rvConsents.layoutManager = LinearLayoutManager(this)
        binding.rvConsents.adapter = ConsentAdapter(consentItems) { item, granted ->
            item.granted = granted
        }
    }

    private fun saveConsents() {
        lifecycleScope.launch {
            var allSaved = true

            for (item in consentItems) {
                if (item.granted) {
                    try {
                        val response = ApiClient.getService().grantConsent(
                            mapOf(
                                "childId" to (prefs.childId ?: ""),
                                "featureName" to item.feature,
                                "policyVersion" to "1.0"
                            )
                        )
                        if (response.isSuccessful) {
                            prefs.setConsentGranted(item.feature, true)
                        } else {
                            allSaved = false
                        }
                    } catch (e: Exception) {
                        // Save locally even if server fails — will sync later
                        prefs.setConsentGranted(item.feature, true)
                    }
                } else {
                    prefs.setConsentGranted(item.feature, false)
                }
            }

            prefs.isOnboarded = true

            Toast.makeText(
                this@ConsentActivity,
                if (allSaved) "Setup complete!" else "Setup complete (some consents will sync later)",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this@ConsentActivity, DashboardActivity::class.java))
            finish()
        }
    }
}

data class ConsentItem(
    val feature: String,
    val title: String,
    val description: String,
    val why: String,
    val whatNot: String,
    val icon: Int,
    var granted: Boolean = false
)

class ConsentAdapter(
    private val items: List<ConsentItem>,
    private val onToggle: (ConsentItem, Boolean) -> Unit
) : RecyclerView.Adapter<ConsentAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvConsentTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvConsentDesc)
        val tvWhy: TextView = view.findViewById(R.id.tvConsentWhy)
        val tvWhatNot: TextView = view.findViewById(R.id.tvConsentWhatNot)
        val btnAllow: Button = view.findViewById(R.id.btnAllow)
        val btnDeny: Button = view.findViewById(R.id.btnDeny)
        val ivIcon: ImageView = view.findViewById(R.id.ivConsentIcon)
        val ivConsentCheck: ImageView = view.findViewById(R.id.ivConsentCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvWhy.text = "✅ Why: ${item.why}"
        holder.tvWhatNot.text = "🚫 ${item.whatNot}"

        updateButtonState(holder, item.granted)

        holder.btnAllow.setOnClickListener {
            item.granted = true
            onToggle(item, true)
            updateButtonState(holder, true)
        }

        holder.btnDeny.setOnClickListener {
            item.granted = false
            onToggle(item, false)
            updateButtonState(holder, false)
        }
    }

    private fun updateButtonState(holder: ViewHolder, granted: Boolean) {
        if (granted) {
            holder.btnAllow.alpha = 1f
            holder.btnDeny.alpha = 0.5f
            holder.ivConsentCheck.visibility = View.VISIBLE
        } else {
            holder.btnAllow.alpha = 0.5f
            holder.btnDeny.alpha = 1f
            holder.ivConsentCheck.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}
