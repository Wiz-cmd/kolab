package com.kolab.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "kolab_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_USER_PUBLIC_KEY = "user_public_key"
    }

    @Volatile
    private var accessTokenMemory: String? = null

    fun saveTokens(accessToken: String, refreshToken: String) {
        accessTokenMemory = accessToken
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
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

    /** E2EE chat public key (SPKI base64) last published to the backend. */
    fun getPublicKey(): String? = prefs.getString(KEY_USER_PUBLIC_KEY, null)

    fun savePublicKey(pub: String) {
        prefs.edit().putString(KEY_USER_PUBLIC_KEY, pub).apply()
    }

    fun clearTokens() {
        accessTokenMemory = null
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
    }

    fun clearAll() {
        accessTokenMemory = null
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getRefreshToken() != null
}
