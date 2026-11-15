package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * A Frames post. Media is optional: text-only posts still work, while a
 * picture or a short video is uploaded first and referenced by URL.
 */
data class CreatePostRequest(
    val description: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    val filter: String? = null,
    val location: String? = null,
    val category: String,
    val tags: List<String> = emptyList()
)

data class PostResponse(
    val id: String,
    val description: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    val filter: String? = null,
    val location: String? = null,
    val category: String,
    val tags: List<String> = emptyList(),
    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("viewsCount") val viewsCount: Int = 0,
    @SerializedName("commentsCount") val commentsCount: Int = 0,
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

data class UploadResponse(
    val url: String,
    val key: String? = null,
    val filename: String? = null,
    val mimetype: String? = null,
    val size: Long = 0,
    val storage: String? = null
)

data class CreateCommentRequest(val content: String)

data class CommentResponse(
    val id: String,
    val content: String,
    @SerializedName("createdAt") val createdAt: String,
    val author: UserResponse
)

data class CommentListResponse(
    val data: List<CommentResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)
