package com.kolab.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Builds an [EncryptedSharedPreferences] instance without ever letting a broken
 * Android Keystore entry take the app down.
 *
 * A master key can become unreadable after an OS upgrade, a factory-reset
 * restore or a corrupted Keystore (typically `KeyStoreException` /
 * `InvalidProtocolBufferException` on cold start). When that happens we delete
 * the unusable prefs file and key, then retry once. If encryption still fails we
 * fall back to plain prefs so the app remains usable; the session is simply
 * re-created on the next login.
 */
object SecurePrefs {

    private const val TAG = "SecurePrefs"

    // androidx.security's default alias. Kept explicit so recovery targets the
    // same entry older installs already created.
    private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"

    fun create(context: Context, fileName: String): SharedPreferences {
        val app = context.applicationContext

        repeat(2) { attempt ->
            try {
                // androidx.security always uses MASTER_KEY_ALIAS as its
                // default alias, which is what recovery below targets.
                val masterKey = MasterKey.Builder(app)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                return EncryptedSharedPreferences.create(
                    app,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (t: Throwable) {
                Log.w(TAG, "Encrypted prefs '$fileName' failed (attempt ${attempt + 1})", t)
                reset(app, fileName)
            }
        }

        Log.e(TAG, "Falling back to unencrypted prefs for '$fileName'")
        return app.getSharedPreferences("${fileName}_plain", Context.MODE_PRIVATE)
    }

    private fun reset(context: Context, fileName: String) {
        runCatching { context.deleteSharedPreferences(fileName) }
            .onFailure { Log.w(TAG, "Could not delete prefs '$fileName'", it) }
        runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MASTER_KEY_ALIAS)
            }
        }.onFailure { Log.w(TAG, "Could not reset the master key", it) }
    }
}
