package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class CreateDealRequest(
    @SerializedName("creatorId") val creatorId: String,
    val title: String,
    val description: String? = null,
    val amount: Double,
    @SerializedName("deliverables") val deliverables: String? = null,
    @SerializedName("deadline") val deadline: String? = null
)

data class DealResponse(
    val id: String,
    val description: String,
    val amount: Double,
    @SerializedName("platformFee") val platformFee: Double,
    @SerializedName("netAmount") val netAmount: Double,
    val status: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("escrowStatus") val escrowStatus: String,
    val deliverables: List<String> = emptyList(),
    val deadline: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    val brand: UserResponse,
    val creator: UserResponse
)

data class DealListResponse(
    val data: List<DealResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)

data class DealStatusRequest(val status: String)

data class DisputeRequest(val reason: String)

data class MessageResponse(
    val id: String,
    val content: String,
    @SerializedName("senderId") val senderId: String,
    @SerializedName("createdAt") val createdAt: String,
    val sender: UserResponse
)

data class MessageListResponse(
    val data: List<MessageResponse>,
    val total: Int
)

data class SendMessageRequest(val content: String)
