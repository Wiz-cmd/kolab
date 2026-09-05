package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class CreatePostRequest(
    val description: String,
    val images: List<String> = emptyList(),
    val video: String? = null,
    val category: String,
    val tags: List<String> = emptyList()
)

data class PostResponse(
    val id: String,
    val description: String,
    val images: List<String> = emptyList(),
    val video: String? = null,
    val category: String,
    val tags: List<String> = emptyList(),
    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("viewsCount") val viewsCount: Int = 0,
    @SerializedName("isLiked") val isLiked: Boolean = false,
    @SerializedName("createdAt") val createdAt: String,
    val author: UserResponse
)

data class PostListResponse(
    val data: List<PostResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)

data class LikeResponse(
    val liked: Boolean,
    @SerializedName("likesCount") val likesCount: Int
)

data class InterestScores(
    val categories: Map<String, Double>,
    val tags: Map<String, Double>,
    val roles: Map<String, Double>,
    val locations: Map<String, Double>
)
