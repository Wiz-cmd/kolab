package com.example.data.security

import android.content.Context
import android.content.SharedPreferences

/**
 * Tracks whether biometric app-lock is requested on this device.
 * Stored in plain [SharedPreferences] on purpose: it is a privacy setting,
 * not a secret (the actual gate is the OS BiometricPrompt / Keyguard).
 */
class AppLockManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kolab_app_lock", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_ENABLED = "biometric_lock_enabled"
    }
}