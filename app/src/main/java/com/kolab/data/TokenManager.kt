package com.kolab.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    // Never throws: falls back to plain prefs if the Keystore entry is broken.
    private val prefs: SharedPreferences = SecurePrefs.create(context, "kolab_secure_prefs")

    companion object {
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_USER_PUBLIC_KEY = "user_public_key"
        private const val KEY_GUEST_MODE = "guest_mode"

        private const val LEGACY_KEY_DEMO_MODE = "demo_mode"
    }

    @Volatile
    private var accessTokenMemory: String? = null

    init {
        // Users upgrading from the old offline demo must not stay signed in
        // with a synthetic token that the real API rejects.
        if (prefs.getBoolean(LEGACY_KEY_DEMO_MODE, false)) {
            prefs.edit().clear().apply()
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        accessTokenMemory = accessToken
        prefs.edit()
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .remove(KEY_GUEST_MODE)
            .apply()
    }

    fun getAccessToken(): String? = accessTokenMemory

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveUserInfo(id: String, name: String, email: String, role: String, avatar: String?) {
        prefs.edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_AVATAR, avatar)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)
    fun getUserAvatar(): String? = prefs.getString(KEY_USER_AVATAR, null)

    // ── Guest session ───────────────────────────────────────────────────
    // Guests can browse public content with no account. They have no tokens,
    // so every protected API call is rejected until they sign in.

    fun isGuestMode(): Boolean = prefs.getBoolean(KEY_GUEST_MODE, false)

    fun enterGuestMode() {
        accessTokenMemory = null
        prefs.edit()
            .putBoolean(KEY_GUEST_MODE, true)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_AVATAR)
            .apply()
    }

    /** E2EE chat public key (SPKI base64) last published to the backend. */
    fun getPublicKey(): String? = prefs.getString(KEY_USER_PUBLIC_KEY, null)

    fun savePublicKey(pub: String) {
        prefs.edit().putString(KEY_USER_PUBLIC_KEY, pub).apply()
    }

    fun clearTokens() {
        accessTokenMemory = null
        prefs.edit()
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_GUEST_MODE)
            .apply()
    }

    fun clearAll() {
        accessTokenMemory = null
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getRefreshToken() != null
}
