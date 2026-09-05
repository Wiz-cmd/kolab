package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val location: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    @SerializedName("isVerified") val isVerified: Boolean = false,
    @SerializedName("isBanned") val isBanned: Boolean = false,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("followersCount") val followersCount: Int = 0,
    @SerializedName("followingCount") val followingCount: Int = 0,
    @SerializedName("postsCount") val postsCount: Int = 0,
    @SerializedName("publicKey") val publicKey: String? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val avatar: String? = null,
    @SerializedName("publicKey") val publicKey: String? = null
)

data class PortfolioItemRequest(
    val title: String,
    val description: String? = null,
    val url: String,
    val type: String = "image"
)

data class PortfolioItemResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val url: String,
    val type: String,
    @SerializedName("createdAt") val createdAt: String
)

data class FollowResponse(val message: String)

data class UserListResponse(
    val data: List<UserResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)
