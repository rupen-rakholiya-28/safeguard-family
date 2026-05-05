package com.childprotection.parent.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.childprotection.parent.R
import com.childprotection.parent.data.ParentPrefs
import com.childprotection.parent.databinding.ActivityMainBinding
import com.childprotection.parent.network.ApiClient
import com.childprotection.parent.ui.alerts.AlertsFragment
import com.childprotection.parent.ui.controls.ControlsFragment
import com.childprotection.parent.ui.family.FamilyFragment
import com.google.android.material.navigation.NavigationBarView

/**
 * Main activity hosting bottom navigation with 4 tabs:
 * Dashboard | Family | Controls | Alerts
 */
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: ParentPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = ParentPrefs(this)
        ApiClient.init(prefs)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)

        // Default tab
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_dashboard -> DashboardFragment()
            R.id.nav_family -> FamilyFragment()
            R.id.nav_controls -> ControlsFragment()
            R.id.nav_alerts -> AlertsFragment()
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun logout() {
        prefs.clearAll()
        startActivity(Intent(this, com.childprotection.parent.ui.auth.AuthActivity::class.java))
        finish()
    }
}
