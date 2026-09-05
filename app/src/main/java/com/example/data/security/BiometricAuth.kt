package com.example.data.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Minimal wrapper around androidx BiometricPrompt.
 *
 * Falls back to the OS device credential when biometrics are unavailable,
 * so "app lock" still works for users who only use a PIN/pattern/password.
 */
object BiometricAuth {

    fun canAuthenticate(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity)
            .canAuthenticate(
                authenticators(activity)
            ) == BiometricManager.BIOMETRIC_SUCCESS ||
            BiometricManager.from(activity)
                .canAuthenticate(
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                ) == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit = { _, _ -> }
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode, errString.toString())
                }
            }
        )

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        }

        prompt.authenticate(builder.build())
    }

    private fun authenticators(activity: FragmentActivity): Int {
        val biometrics = BiometricManager.from(activity)
        val weakOk = biometrics.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            if (weakOk) BiometricManager.Authenticators.BIOMETRIC_WEAK else BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
    }
}