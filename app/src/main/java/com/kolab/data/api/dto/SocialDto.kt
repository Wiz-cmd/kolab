package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class SocialStartResponse(
    @SerializedName("configured") val configured: Boolean = false,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("authUrl") val authUrl: String? = null,
    @SerializedName("state") val state: String? = null
)

data class SocialExchangeRequest(
    val code: String,
    val state: String
)