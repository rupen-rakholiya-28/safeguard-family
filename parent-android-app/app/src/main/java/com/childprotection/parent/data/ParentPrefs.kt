package com.childprotection.parent.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted storage for parent auth tokens and family data.
 * AGENTS.md: Encrypt sensitive data, never store unnecessary personal data.
 */
class ParentPrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "parent_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var displayName: String?
        get() = prefs.getString(KEY_NAME, null)
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var familyId: String?
        get() = prefs.getString(KEY_FAMILY_ID, null)
        set(value) = prefs.edit().putString(KEY_FAMILY_ID, value).apply()

    var familyName: String?
        get() = prefs.getString(KEY_FAMILY_NAME, null)
        set(value) = prefs.edit().putString(KEY_FAMILY_NAME, value).apply()

    var inviteCode: String?
        get() = prefs.getString(KEY_INVITE_CODE, null)
        set(value) = prefs.edit().putString(KEY_INVITE_CODE, value).apply()

    val isLoggedIn: Boolean get() = accessToken != null

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_FAMILY_ID = "family_id"
        private const val KEY_FAMILY_NAME = "family_name"
        private const val KEY_INVITE_CODE = "invite_code"
    }
}
