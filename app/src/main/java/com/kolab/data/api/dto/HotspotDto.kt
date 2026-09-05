package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class HotspotResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: String,
    val city: String,
    val province: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val image: String? = null,
    @SerializedName("checkInsCount") val checkInsCount: Int = 0,
    @SerializedName("createdAt") val createdAt: String
)

data class HotspotListResponse(
    val data: List<HotspotResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)

data class CheckInResponse(
    val message: String,
    @SerializedName("checkInsCount") val checkInsCount: Int
)

data class LeaderboardEntry(
    val province: String,
    @SerializedName("totalCheckIns") val totalCheckIns: Int,
    @SerializedName("uniqueUsers") val uniqueUsers: Int,
    @SerializedName("topHotspot") val topHotspot: HotspotResponse? = null
)

data class CreateHotspotRequest(
    val name: String,
    val description: String? = null,
    val category: String,
    val city: String,
    val province: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)
