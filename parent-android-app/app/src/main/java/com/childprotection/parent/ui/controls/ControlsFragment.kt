package com.childprotection.parent.ui.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.FragmentControlsBinding
import com.childprotection.parent.network.ApiClient
import com.childprotection.parent.network.PolicyData
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Parental controls: Screen time limits, app blocking, bedtime, study mode.
 * AGENTS.md: No hidden behavior. All controls are transparent to the child.
 */
class ControlsFragment : Fragment() {

    private var _binding: FragmentControlsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: ParentPrefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = ParentPrefs(requireContext())

        _binding?.rvPolicies?.layoutManager = LinearLayoutManager(context)

        // Policy type spinner
        val policyTypes = arrayOf("SCREEN_TIME_LIMIT", "APP_BLOCK", "BEDTIME_MODE", "STUDY_MODE")
        _binding?.spinnerPolicyType?.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, policyTypes
        )

        _binding?.btnAddRule?.setOnClickListener { createPolicy() }

        loadPolicies()
    }

    private fun createPolicy() {
        val type = _binding?.spinnerPolicyType?.selectedItem.toString()
        val limitStr = _binding?.etDailyLimit?.text.toString().trim()
        val start = _binding?.etStartTime?.text.toString().trim()
        val end = _binding?.etEndTime?.text.toString().trim()

        val body = mutableMapOf<String, Any?>(
            "policyType" to type,
            "active" to true
        )

        if (limitStr.isNotEmpty()) body["dailyLimitMinutes"] = limitStr.toIntOrNull() ?: 120
        if (start.isNotEmpty()) body["startTime"] = start
        if (end.isNotEmpty()) body["endTime"] = end

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.getService().createPolicy(body)
                if (res.isSuccessful) {
                    if (isAdded) {
                        Toast.makeText(context, "Rule added!", Toast.LENGTH_SHORT).show()
                        _binding?.etDailyLimit?.text?.clear()
                        _binding?.etStartTime?.text?.clear()
                        _binding?.etEndTime?.text?.clear()
                    }
                    loadPolicies()
                } else {
                    if (isAdded) {
                        Toast.makeText(context, res.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadPolicies() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.getService().getPolicies("")
                if (res.isSuccessful && isAdded) {
                    val policies = res.body()?.data ?: emptyList()
                    if (policies.isEmpty()) {
                        _binding?.tvNoPolicies?.visibility = View.VISIBLE
                        _binding?.rvPolicies?.visibility = View.GONE
                    } else {
                        _binding?.tvNoPolicies?.visibility = View.GONE
                        _binding?.rvPolicies?.visibility = View.VISIBLE
                        _binding?.rvPolicies?.adapter = PoliciesAdapter(policies) { policy ->
                            confirmDeletePolicy(policy)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail, user can pull to refresh
            }
        }
    }

    private fun confirmDeletePolicy(policy: PolicyData) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Rule?")
            .setMessage("Remove this ${policy.policyType.replace("_", " ")} rule?")
            .setPositiveButton("Delete") { _, _ -> deletePolicy(policy.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePolicy(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.getService().deletePolicy(id)
                loadPolicies()
                if (isAdded) {
                    Toast.makeText(context, "Rule deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PoliciesAdapter(
    private val policies: List<PolicyData>,
    private val onDelete: (PolicyData) -> Unit
) : RecyclerView.Adapter<PoliciesAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvPolicyType)
        val tvDetails: TextView = view.findViewById(R.id.tvPolicyDetails)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDeletePolicy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_policy, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = policies[position]
        val icon = when (p.policyType) {
            "SCREEN_TIME_LIMIT" -> "⏱️"
            "APP_BLOCK" -> "🚫"
            "BEDTIME_MODE" -> "🌙"
            "STUDY_MODE" -> "📚"
            else -> "📋"
        }
        holder.tvType.text = "$icon ${p.policyType.replace("_", " ")}"

        val details = buildString {
            p.dailyLimitMinutes?.let { append("Limit: ${it}min  ") }
            if (!p.startTime.isNullOrEmpty()) append("${p.startTime} - ${p.endTime}")
        }
        holder.tvDetails.text = details.ifEmpty { "Active" }
        holder.btnDelete.setOnClickListener { onDelete(p) }
    }

    override fun getItemCount() = policies.size
}
