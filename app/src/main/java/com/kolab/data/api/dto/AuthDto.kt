package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String? = null,
    val location: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("twoFactorCode") val twoFactorCode: String? = null
)

data class TokenPair(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class PasswordResetRequest(val email: String)

data class ResetPasswordRequest(val token: String, val password: String)

data class TwoFactorSetupResponse(
    @SerializedName("secret") val secret: String,
    @SerializedName("otpauthUrl") val otpauthUrl: String
)

data class TwoFactorEnableRequest(val code: String)
