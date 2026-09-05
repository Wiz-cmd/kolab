package com.kolab.data

import java.util.UUID

data class UiUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = "",
    val role: String = "CREATOR",
    val location: String = "",
    val avatar: String = "",
    val bio: String = "",
    val isVerified: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0
)

data class UiPost(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val images: List<String> = emptyList(),
    val video: String? = null,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val viewsCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String = "",
    val author: UiUser = UiUser()
)

data class UiDeal(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val amount: Double = 0.0,
    val platformFee: Double = 0.0,
    val netAmount: Double = 0.0,
    val status: String = "PENDING",
    val paymentStatus: String = "UNPAID",
    val escrowStatus: String = "NONE",
    val deliverables: List<String> = emptyList(),
    val deadline: String? = null,
    val createdAt: String = "",
    val brand: UiUser = UiUser(),
    val creator: UiUser = UiUser()
)

data class UiMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val senderId: String = "",
    val createdAt: String = "",
    val sender: UiUser = UiUser()
)

data class UiHotspot(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val city: String = "",
    val province: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val image: String = "",
    val checkInsCount: Int = 0
)

data class UiLeaderboardEntry(
    val province: String = "",
    val totalCheckIns: Int = 0,
    val uniqueUsers: Int = 0,
    val topHotspot: UiHotspot? = null
)

data class UiNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)

data class UiWithdrawal(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val method: String = "",
    val status: String = "PENDING",
    val createdAt: String = ""
)

data class UiRateCard(
    val id: String = UUID.randomUUID().toString(),
    val category: String = "",
    val description: String = "",
    val amount: Double = 0.0
)

data class UiCollection(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val items: List<com.kolab.data.api.dto.PortfolioItemResponse> = emptyList()
)

data class UiAnalytics(
    val profileViews: Int = 0,
    val postViews: Int = 0,
    val totalLikes: Int = 0,
    val totalCollabTaps: Int = 0,
    val followerGrowth: Int = 0
)

data class UiVerification(
    val status: String = "NONE",
    val submittedAt: String? = null,
    val reviewedAt: String? = null
)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: UiUser? = null,
    val error: String? = null
)

data class FeedState(
    val posts: List<UiPost> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val feedType: FeedType = FeedType.FOR_YOU
)

enum class FeedType { CHRONOLOGICAL, FOR_YOU, PUBLIC }

data class DiscoverState(
    val users: List<UiUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedRole: String? = null,
    val selectedLocation: String? = null
)

data class PlacesState(
    val hotspots: List<UiHotspot> = emptyList(),
    val leaderboard: List<UiLeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedProvince: String? = null,
    val selectedCategory: String? = null
)

data class ProfileState(
    val user: UiUser? = null,
    val isOwnProfile: Boolean = true,
    val posts: List<UiPost> = emptyList(),
    val deals: List<UiDeal> = emptyList(),
    val analytics: UiAnalytics = UiAnalytics(),
    val verification: UiVerification = UiVerification(),
    val isLoading: Boolean = false,
    val error: String? = null
)
