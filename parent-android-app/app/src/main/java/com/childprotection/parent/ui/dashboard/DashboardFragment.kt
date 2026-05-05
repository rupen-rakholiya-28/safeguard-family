package com.childprotection.parent.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.FragmentDashboardBinding
import com.childprotection.parent.network.ApiClient
import kotlinx.coroutines.launch

/**
 * Dashboard fragment: Family overview, screen time stats, alert count.
 * AGENTS.md: Keep UI and logic separated. No hidden behavior.
 */
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

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        binding.swipeRefresh.setOnRefreshListener { loadDashboardData() }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val familyId = prefs.familyId

        if (familyId == null) {
            binding.cardNoFamily.visibility = View.VISIBLE
            binding.statsContainer.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            return
        }

        binding.cardNoFamily.visibility = View.GONE
        binding.statsContainer.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Fetch family members
                val membersRes = ApiClient.getService().getFamilyMembers(familyId)
                if (membersRes.isSuccessful) {
                    val members = membersRes.body()?.data ?: emptyList()
                    val children = members.filter { it.role == "CHILD" }
                    binding.tvChildCount.text = "${children.size}"
                    binding.tvMemberCount.text = "${members.size} members"
                    binding.tvFamilyName.text = prefs.familyName ?: "My Family"
                }

                // Fetch alerts
                val alertsRes = ApiClient.getService().getAlerts(familyId, unacknowledgedOnly = true)
                if (alertsRes.isSuccessful) {
                    val alerts = alertsRes.body()?.data ?: emptyList()
                    binding.tvAlertCount.text = "${alerts.size}"
                    val sosCount = alerts.count { it.alertType == "SOS" }
                    if (sosCount > 0) {
                        binding.tvAlertBadge.text = "⚠️ $sosCount SOS!"
                        binding.tvAlertBadge.visibility = View.VISIBLE
                    } else {
                        binding.tvAlertBadge.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Refresh failed", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
