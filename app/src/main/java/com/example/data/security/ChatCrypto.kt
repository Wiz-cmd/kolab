package com.example.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Client-side E2EE for chat messages.
 *
 * Each install owns an EC P-256 key pair (private half never leaves the app,
 * persisted inside EncryptedSharedPreferences which is itself wrapped in the
 * Android Keystore). Sending encrypts with AES-256-GCM using a key derived via
 * ECDH(my-private, peer-public) + HMAC-SHA256 KDF. The other participant
 * derives the identical key from (their-private, my-public), so the backend
 * socket layer only ever sees opaque ciphertext envelopes and cannot read or
 * mutate messages in flight.
 *
 * Every message rides as a JSON envelope: `{"v":1,"iv":"<b64>","ct":"<b64>"}`.
 * Legacy/plaintext messages (sent before both sides had keys) are not wrapped
 * and display as-is.
 */
class ChatCrypto(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "kolab_e2ee_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val keyFactory = KeyFactory.getInstance("EC")

    fun getOrCreateKeyPair(): KeyPair {
        val savedPub = prefs.getString(PREF_PUB, null)
        val savedPri = prefs.getString(PREF_PRI, null)
        if (!savedPub.isNullOrBlank() && !savedPri.isNullOrBlank()) {
            return try {
                KeyPair(
                    keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(savedPub, Base64.DEFAULT))),
                    keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(savedPri, Base64.DEFAULT)))
                )
            } catch (e: Exception) {
                generateAndStore()
            }
        }
        return generateAndStore()
    }

    /** This device's public key, SPKI DER base64 (what gets published to /users/me). */
    fun myPublicKey(): String =
        Base64.encodeToString(getOrCreateKeyPair().public.encoded, Base64.NO_WRAP)

    /**
     * Encrypt [plaintext] for the peer who owns [peerPublicKeyB64].
     * Returns the JSON envelope as a string, or null when the peer has no key
     * published yet (caller then falls back to sending plaintext with a notice).
     */
    fun encrypt(peerPublicKeyB64: String?, plaintext: String): String? {
        if (peerPublicKeyB64.isNullOrBlank()) return null
        return try {
            val peer = decodePublic(peerPublicKeyB64)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
            cipher.init(Cipher.ENCRYPT_MODE, deriveSecret(peer), GCMParameterSpec(128, iv))
            val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            JSONObject()
                .put("v", 1)
                .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                .put("ct", Base64.encodeToString(ct, Base64.NO_WRAP))
                .toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypt [envelope] produced by the peer who owns [senderPublicKeyB64].
     * Returns plaintext, or null when the envelope is malformed / tampered /
     * the peer's key is unknown.
     */
    fun decrypt(senderPublicKeyB64: String?, envelope: String): String? {
        if (senderPublicKeyB64.isNullOrBlank()) return null
        return try {
            val peer = decodePublic(senderPublicKeyB64)
            val obj = JSONObject(envelope)
            if (obj.optInt("v", 0) != 1) return null
            val iv = Base64.decode(obj.optString("iv"), Base64.DEFAULT)
            val ct = Base64.decode(obj.optString("ct"), Base64.DEFAULT)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, deriveSecret(peer), GCMParameterSpec(128, iv))
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun isEnvelope(content: String): Boolean =
        content.isNotBlank() && content.startsWith("{") && content.contains("\"v\":1")

    private fun deriveSecret(peer: PublicKey): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(getOrCreateKeyPair().private)
        keyAgreement.doPhase(peer, true)
        val shared = keyAgreement.generateSecret()

        // HKDF-lite: HMAC-SHA256(sharedSecret, info) → 32-byte AES-256 key.
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(shared, "HmacSHA256"))
        val key = mac.doFinal(KOLAB_INFO.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(key, "AES")
    }

    private fun decodePublic(b64: String): PublicKey =
        keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(b64, Base64.DEFAULT)))

    private fun generateAndStore(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        val keyPair = generator.generateKeyPair()
        prefs.edit()
            .putString(PREF_PUB, Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP))
            .putString(PREF_PRI, Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP))
            .apply()
        return keyPair
    }

    companion object {
        private const val PREF_PUB = "ec_public_key"
        private const val PREF_PRI = "ec_private_key"
        private const val KOLAB_INFO = "kolab-e2ee-v1"
    }
}