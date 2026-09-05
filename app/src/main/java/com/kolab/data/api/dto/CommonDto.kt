package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: String,
    val data: Map<String, Any>? = null
)

data class NotificationListResponse(
    val data: List<NotificationResponse>,
    val total: Int
)

data class UnreadCountResponse(@SerializedName("count") val count: Int)

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    val platform: String = "android"
)

data class VerificationStatus(
    val status: String,
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
    val reason: String? = null
)

data class VerificationSubmitRequest(
    @SerializedName("idDocument") val idDocument: String,
    val portfolio: List<String> = emptyList()
)

data class RateCardResponse(
    val id: String,
    val category: String,
    val description: String? = null,
    val amount: Double,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateRateCardRequest(
    val category: String,
    val description: String? = null,
    val amount: Double
)

data class CollectionResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val items: List<PortfolioItemResponse> = emptyList(),
    @SerializedName("createdAt") val createdAt: String
)

data class CreateCollectionRequest(
    val name: String,
    val description: String? = null
)

data class CollectionItemRequest(
    @SerializedName("portfolioItemId") val portfolioItemId: String
)

data class AnalyticsSummary(
    @SerializedName("profileViews") val profileViews: Int = 0,
    @SerializedName("postViews") val postViews: Int = 0,
    @SerializedName("totalLikes") val totalLikes: Int = 0,
    @SerializedName("totalCollabTaps") val totalCollabTaps: Int = 0,
    @SerializedName("followerGrowth") val followerGrowth: Int = 0,
    val period: String = "30d"
)

data class ReportRequest(
    val reason: String,
    val description: String? = null
)

data class ContractResponse(
    val id: String,
    @SerializedName("dealId") val dealId: String,
    val pdfUrl: String? = null,
    @SerializedName("createdAt") val createdAt: String
)

data class AlertResponse(
    val id: String,
    val query: String,
    @SerializedName("isEnabled") val isEnabled: Boolean = true,
    @SerializedName("createdAt") val createdAt: String
)

data class SavedSearchResponse(
    val id: String,
    val query: String,
    val filters: Map<String, Any>? = null,
    @SerializedName("isEnabled") val isEnabled: Boolean = true,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateSavedSearchRequest(
    val query: String,
    val filters: Map<String, Any>? = null
)
