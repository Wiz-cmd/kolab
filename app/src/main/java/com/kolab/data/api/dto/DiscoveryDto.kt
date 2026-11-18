package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class ChallengeResponse(
    val tag: String,
    val title: String,
    val city: String,
    val brief: String,
    val week: Int,
    val year: Int,
    @SerializedName("endsAt") val endsAt: String
)

data class ProfileViewersResponse(
    val enabled: Boolean = true,
    val viewers: List<ProfileViewerResponse> = emptyList()
)

data class ProfileViewerResponse(
    val id: String,
    val name: String = "",
    @SerializedName("avatarUrl") val avatar: String? = null,
    val role: String = "",
    val location: String? = null,
    val visits: Int = 0,
    @SerializedName("lastViewedAt") val lastViewedAt: String? = null
)
