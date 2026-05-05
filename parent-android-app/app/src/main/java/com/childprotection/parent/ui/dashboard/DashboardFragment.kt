package com.childprotection.parent.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.FragmentDashboardBinding
import com.childprotection.parent.network.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: ParentPrefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = ParentPrefs(requireContext())

        binding.tvGreeting.text = "Hi, ${prefs.displayName ?: "Parent"} 👋"
        binding.tvFamilyName.text = prefs.familyName ?: "Family Dashboard"

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        binding.swipeRefresh.setOnRefreshListener { loadDashboardData() }
        
        // Quick action clicks
        binding.cardControls.setOnClickListener {
            // Navigate to controls - handled by bottom nav
        }
        
        binding.cardFamily.setOnClickListener {
            // Navigate to family - handled by bottom nav
        }
        
        binding.cardAlerts.setOnClickListener {
            // Navigate to alerts - handled by bottom nav
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val familyId = prefs.familyId

        if (familyId == null) {
            _binding?.cardNoFamily?.visibility = View.VISIBLE
            _binding?.statsContainer?.visibility = View.GONE
            _binding?.swipeRefresh?.isRefreshing = false
            return
        }

        _binding?.cardNoFamily?.visibility = View.GONE
        _binding?.statsContainer?.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch family members
                val membersRes = ApiClient.getService().getFamilyMembers(familyId)
                if (membersRes.isSuccessful && isAdded) {
                    val members = membersRes.body()?.data ?: emptyList()
                    val children = members.filter { it.role == "CHILD" }
                    
                    binding.tvChildCount.text = "${children.size}"
                    binding.tvMemberCount.text = "${members.size}"
                    binding.tvFamilyNameSmall.text = prefs.familyName ?: "My Family"
                    
                    // Load child data if exists
                    if (children.isNotEmpty()) {
                        loadChildData(children.first())
                    }
                }

                // Fetch alerts
                val alertsRes = ApiClient.getService().getAlerts(familyId)
                if (alertsRes.isSuccessful && isAdded) {
                    val alerts = alertsRes.body()?.data ?: emptyList()
                    val unacknowledged = alerts.filter { !it.acknowledged }
                    
                    binding.tvAlertCount.text = "${unacknowledged.size}"
                    
                    if (unacknowledged.isNotEmpty()) {
                        binding.tvAlertBadge.visibility = View.VISIBLE
                        binding.tvAlertBadge.text = "${unacknowledged.size} new"
                        binding.tvLatestAlert.text = unacknowledged.first().title
                    } else {
                        binding.tvAlertBadge.visibility = View.GONE
                        binding.tvLatestAlert.text = "No alerts - all good!"
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    // Silent fail - data might load later
                }
            } finally {
                if (isAdded) {
                    _binding?.swipeRefresh?.isRefreshing = false
                }
            }
        }
    }
    
    private fun loadChildData(child: com.childprotection.parent.network.MemberData) {
        binding.tvChild1Name.text = child.displayName
        binding.tvChild1Status.text = "Active"
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Screen Time
                val screenTimeRes = ApiClient.getService().getScreenTime(child.id)
                if (screenTimeRes.isSuccessful && isAdded) {
                    val data = screenTimeRes.body()?.data as? JsonObject
                    if (data != null) {
                        val totalMinutes = data.get("totalMinutes")?.asInt ?: 0
                        val hours = totalMinutes / 60
                        val mins = totalMinutes % 60
                        binding.tvChild1ScreenTime.text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        
                        // App count
                        val apps = data.get("apps")?.asJsonArray
                        binding.tvChild1Apps.text = "${apps?.size() ?: 0}"
                        
                        // Show top apps
                        showAppUsage(apps)
                    } else {
                        binding.tvChild1ScreenTime.text = "0m"
                        binding.tvChild1Apps.text = "0"
                    }
                }
                
                // Location
                val locationRes = ApiClient.getService().getLatestLocation(child.id)
                if (locationRes.isSuccessful && isAdded) {
                    val location = locationRes.body()?.data as? JsonObject
                    if (location != null) {
                        binding.tvChild1Location.text = "📍 Live"
                    } else {
                        binding.tvChild1Location.text = "--"
                    }
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    private fun showAppUsage(apps: com.google.gson.JsonArray?) {
        val layout = binding.layoutAppUsage
        layout.removeAllViews()
        
        if (apps == null || apps.size() == 0) {
            binding.tvNoAppUsage.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoAppUsage.visibility = View.GONE
        
        val maxCount = minOf(apps.size(), 5)
        for (i in 0 until maxCount) {
            val app = apps.get(i) as? JsonObject ?: continue
            val appName = app.get("appName")?.asString ?: "Unknown"
            val duration = app.get("durationMinutes")?.asInt ?: 0
            
            val itemView = layoutInflater.inflate(R.layout.item_app_usage, layout, false)
            itemView.findViewById<TextView>(R.id.tvAppName).text = appName
            itemView.findViewById<TextView>(R.id.tvAppDuration).text = "${duration}m"
            
            // Calculate progress width (max 2 hours = 120 min)
            val progress = (duration * 100 / 120).coerceIn(0, 100)
            itemView.findViewById<View>(R.id.progressBar).layoutParams = 
                (itemView.findViewById<View>(R.id.progressBar).layoutParams as LinearLayout.LayoutParams).apply {
                    width = (progress * resources.displayMetrics.density * 2).toInt()
            }
            
            layout.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}