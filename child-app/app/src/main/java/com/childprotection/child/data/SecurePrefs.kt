package com.childprotection.child.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure local storage for auth tokens, consent state, and family info.
 * Uses EncryptedSharedPreferences for sensitive data protection.
 */
class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "safeguard_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var childId: String?
        get() = prefs.getString(KEY_CHILD_ID, null)
        set(value) = prefs.edit().putString(KEY_CHILD_ID, value).apply()

    var familyId: String?
        get() = prefs.getString(KEY_FAMILY_ID, null)
        set(value) = prefs.edit().putString(KEY_FAMILY_ID, value).apply()

    var familyName: String?
        get() = prefs.getString(KEY_FAMILY_NAME, null)
        set(value) = prefs.edit().putString(KEY_FAMILY_NAME, value).apply()

    var childName: String?
        get() = prefs.getString(KEY_CHILD_NAME, null)
        set(value) = prefs.edit().putString(KEY_CHILD_NAME, value).apply()

    var deviceId: String?
        get() = prefs.getString(KEY_DEVICE_ID, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()

    // Consent states - stored locally for offline enforcement
    fun setConsentGranted(feature: String, granted: Boolean) {
        prefs.edit().putBoolean("consent_$feature", granted).apply()
    }



    fun isConsentGranted(feature: String): Boolean {
        return prefs.getBoolean("consent_$feature", false)
    }

    // Known apps set for on-device risk detection

    fun getKnownApps(): Set<String> {
        return prefs.getStringSet(KEY_KNOWN_APPS, emptySet()) ?: emptySet()
    }

fun addKnownApps(apps: Set<String>) {
        val current = getKnownApps().toMutableSet()
        current.addAll(apps)
        prefs.edit().putStringSet(KEY_KNOWN_APPS, current).apply()
    }

    /**
     * Check if both global config AND user consent allow a feature.
     * Usage: if (!prefs.isFeatureAllowed("LIVE_VOICE")) return
     */
    fun isFeatureAllowed(featureKey: String): Boolean {
        return try {
            val config = com.childprotection.child.config.ConsentConfig
            when (featureKey) {
                "SCREEN_TIME" -> config.SCREEN_TIME_TRACKING_ENABLED && isConsentGranted("SCREEN_TIME_TRACKING")
                "APP_USAGE" -> config.APP_USAGE_TRACKING_ENABLED && isConsentGranted("APP_USAGE_TRACKING")
                "LOCATION" -> config.LOCATION_SHARING_ENABLED && isConsentGranted("LOCATION_SHARING")
                "RISK_DETECTION" -> config.RISK_DETECTION_ENABLED && isConsentGranted("RISK_DETECTION")
                "LIVE_VOICE" -> config.LIVE_VOICE_HELP_ENABLED && isConsentGranted("LIVE_SUPPORT")
                "LIVE_SCREEN" -> config.LIVE_SCREEN_SHARE_ENABLED && isConsentGranted("LIVE_SUPPORT")
                "SOS" -> config.SOS_ENABLED
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }


    val isLoggedIn: Boolean
        get() = accessToken != null && familyId != null

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_CHILD_ID = "child_id"
        private const val KEY_FAMILY_ID = "family_id"
        private const val KEY_FAMILY_NAME = "family_name"
        private const val KEY_CHILD_NAME = "child_name"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ONBOARDED = "is_onboarded"
        private const val KEY_KNOWN_APPS = "known_apps"
    }
}
