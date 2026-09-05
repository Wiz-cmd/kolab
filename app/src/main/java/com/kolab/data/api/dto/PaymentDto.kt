package com.kolab.data.api.dto

import com.google.gson.annotations.SerializedName

data class WithdrawalRequest(
    val amount: Double,
    val method: String,
    @SerializedName("accountNumber") val accountNumber: String? = null,
    @SerializedName("bankName") val bankName: String? = null,
    @SerializedName("branchCode") val branchCode: String? = null
)

data class WithdrawalResponse(
    val id: String,
    val amount: Double,
    val method: String,
    val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("processedAt") val processedAt: String? = null
)

data class WithdrawalListResponse(
    val data: List<WithdrawalResponse>,
    val total: Int,
    val take: Int,
    val skip: Int
)

data class PayFastUrlResponse(val url: String)

data class OzowParamsResponse(
    @SerializedName("checkoutUrl") val checkoutUrl: String,
    val token: String
)
