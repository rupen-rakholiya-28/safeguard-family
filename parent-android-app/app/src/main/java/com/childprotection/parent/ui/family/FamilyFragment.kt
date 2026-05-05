package com.childprotection.parent.ui.family

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.FragmentFamilyBinding
import com.childprotection.parent.network.ApiClient
import com.childprotection.parent.network.MemberData
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Family management: Create family, view invite code, list members, manage consents.
 * AGENTS.md: Consent-first. No feature works without consent.
 */
class FamilyFragment : Fragment() {

    private var _binding: FragmentFamilyBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: ParentPrefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFamilyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = ParentPrefs(requireContext())

        binding.rvMembers.layoutManager = LinearLayoutManager(context)

        if (prefs.familyId == null) {
            showCreateFamily()
        } else {
            showFamilyDetails()
        }
    }

    private fun showCreateFamily() {
        _binding?.layoutNoFamily?.visibility = View.VISIBLE
        _binding?.layoutFamilyDetails?.visibility = View.GONE

        _binding?.btnCreateFamily?.setOnClickListener {
            val name = _binding?.etFamilyName?.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(context, "Enter a family name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createFamily(name)
        }
    }

    private fun createFamily(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.getService().createFamily(mapOf("name" to name))
                if (res.isSuccessful && res.body()?.success == true) {
                    val data = res.body()!!.data!!
                    prefs.familyId = data.id
                    prefs.familyName = data.name
                    prefs.inviteCode = data.inviteCode
                    if (isAdded) {
                        showFamilyDetails()
                        Toast.makeText(context, "Family created!", Toast.LENGTH_SHORT).show()
                    }
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

    private fun showFamilyDetails() {
        _binding?.layoutNoFamily?.visibility = View.GONE
        _binding?.layoutFamilyDetails?.visibility = View.VISIBLE

        _binding?.tvFamilyTitle?.text = prefs.familyName ?: "My Family"
        _binding?.tvInviteCode?.text = prefs.inviteCode ?: "------"

        _binding?.btnCopyCode?.setOnClickListener {
            val clip = ClipData.newPlainText("Invite Code", prefs.inviteCode)
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(clip)
            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
        }

        _binding?.btnShareCode?.setOnClickListener {
            val shareText = "Join our family on SafeGuard! Use code: ${prefs.inviteCode}\n\nDownload: https://play.google.com/store/apps/details?id=com.childprotection.child"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share invite code"))
        }

        loadMembers()
    }

    private fun loadMembers() {
        val familyId = prefs.familyId ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.getService().getFamilyMembers(familyId)
                if (res.isSuccessful && isAdded) {
                    val members = res.body()?.data ?: emptyList()
                    _binding?.rvMembers?.adapter = MembersAdapter(members) { member ->
                        if (member.role == "CHILD") showChildConsents(member)
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load members", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showChildConsents(child: MemberData) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.getService().getConsents(child.id)
                if (res.isSuccessful && isAdded) {
                    val consents = res.body()?.data ?: emptyList()
                    val message = if (consents.isEmpty()) {
                        "No consents set for ${child.displayName} yet."
                    } else {
                        consents.joinToString("\n") { c ->
                            val icon = if (c.status == "GRANTED") "✅" else "❌"
                            "$icon ${c.featureName.replace("_", " ")}"
                        }
                    }

                    if (isAdded && context != null) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Consents: ${child.displayName}")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load consents", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Simple adapter for members list
class MembersAdapter(
    private val members: List<MemberData>,
    private val onClick: (MemberData) -> Unit
) : RecyclerView.Adapter<MembersAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMemberName)
        val tvRole: TextView = view.findViewById(R.id.tvMemberRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = members[position]
        val emoji = if (m.role == "PARENT") "👨‍👩‍👧" else "👧"
        holder.tvName.text = "$emoji ${m.displayName}"
        holder.tvRole.text = m.role
        holder.itemView.setOnClickListener { onClick(m) }
    }

    override fun getItemCount() = members.size
}
