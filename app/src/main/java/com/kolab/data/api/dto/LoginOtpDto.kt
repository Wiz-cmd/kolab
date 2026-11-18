package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("challenge") val challenge: Boolean = false,
    @SerializedName("challengeId") val challengeId: String? = null,
    @SerializedName("channel") val channel: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("expiresIn") val expiresIn: Int = 0,
    @SerializedName("devCode") val devCode: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null
)

data class VerifyLoginOtpRequest(
    @SerializedName("challengeId") val challengeId: String,
    @SerializedName("code") val code: String
)

data class ResendLoginOtpRequest(
    @SerializedName("challengeId") val challengeId: String
)

data class LoginOtpSettingsRequest(
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("channel") val channel: String,
    @SerializedName("phone") val phone: String? = null
)

data class ResendLoginOtpResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("expiresIn") val expiresIn: Int = 0,
    @SerializedName("devCode") val devCode: String? = null
)
