package com.childprotection.parent.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.FragmentAlertsBinding
import com.childprotection.parent.network.AlertData
import com.childprotection.parent.network.ApiClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Alerts fragment: View safety alerts (SOS, screen time, geofence).
 * AGENTS.md: Clear disconnect flow. Transparent alert system.
 */
class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: ParentPrefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = ParentPrefs(requireContext())

        binding.rvAlerts.layoutManager = LinearLayoutManager(context)
        binding.swipeRefresh.setOnRefreshListener { loadAlerts() }

        loadAlerts()
    }

    private fun loadAlerts() {
        val familyId = prefs.familyId
        if (familyId == null) {
            binding.tvNoAlerts.visibility = View.VISIBLE
            binding.rvAlerts.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            return
        }

        lifecycleScope.launch {
            try {
                val res = ApiClient.getService().getAlerts(familyId)
                if (res.isSuccessful) {
                    val alerts = res.body()?.data ?: emptyList()
                    if (alerts.isEmpty()) {
                        binding.tvNoAlerts.visibility = View.VISIBLE
                        binding.rvAlerts.visibility = View.GONE
                    } else {
                        binding.tvNoAlerts.visibility = View.GONE
                        binding.rvAlerts.visibility = View.VISIBLE
                        binding.rvAlerts.adapter = AlertsAdapter(alerts) { alert ->
                            acknowledgeAlert(alert)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load alerts", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun acknowledgeAlert(alert: AlertData) {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService().acknowledgeAlert(alert.id)
                if (res.isSuccessful) {
                    Toast.makeText(context, "Alert acknowledged", Toast.LENGTH_SHORT).show()
                    loadAlerts()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class AlertsAdapter(
    private val alerts: List<AlertData>,
    private val onAcknowledge: (AlertData) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvAlertTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvAlertMessage)
        val tvSeverity: TextView = view.findViewById(R.id.tvAlertSeverity)
        val tvTime: TextView = view.findViewById(R.id.tvAlertTime)
        val btnAck: MaterialButton = view.findViewById(R.id.btnAcknowledge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = alerts[position]
        val icon = when (a.alertType) {
            "SOS" -> "🆘"
            "SCREEN_TIME_EXCEEDED" -> "⏱️"
            "GEOFENCE" -> "📍"
            else -> "⚠️"
        }
        holder.tvTitle.text = "$icon ${a.title}"
        holder.tvMessage.text = a.message ?: ""
        holder.tvSeverity.text = a.severity

        val severityColor = when (a.severity) {
            "CRITICAL" -> android.graphics.Color.parseColor("#EF4444")
            "HIGH" -> android.graphics.Color.parseColor("#F59E0B")
            else -> android.graphics.Color.parseColor("#3B82F6")
        }
        holder.tvSeverity.setTextColor(severityColor)

        holder.tvTime.text = a.createdAt?.take(16)?.replace("T", " ") ?: ""

        if (a.acknowledged) {
            holder.btnAck.text = "✅ Acknowledged"
            holder.btnAck.isEnabled = false
            holder.btnAck.alpha = 0.5f
        } else {
            holder.btnAck.text = "Acknowledge"
            holder.btnAck.isEnabled = true
            holder.btnAck.alpha = 1f
            holder.btnAck.setOnClickListener { onAcknowledge(a) }
        }
    }

    override fun getItemCount() = alerts.size
}
