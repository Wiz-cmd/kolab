package com.example.data

data class CreativeProfile(
    val id: String,
    val name: String,
    val role: String,
    val location: String,
    val category: String,
    val rate: String,
    val rating: Double,
    val reviewCount: Int = 42,
    val imageUrl: String,
    val avatarUrl: String = imageUrl,
    val bio: String = "Fashion Model & Creative Director based in South Africa. Working on editorial, high-concept, and commercial productions.",
    val followers: String = "14.2K",
    val collabsCount: Int = 86,
    val measurements: Measurements = Measurements(
        height = "178 cm / 5'10\"",
        bust = "84 cm / 33\"",
        waist = "61 cm / 24\"",
        hips = "89 cm / 35\""
    ),
    val availabilityStatus: String = "Open",
    val nextAvailableDate: String = "Oct 15",
    val startingRateHourly: String = "Starting at R2,500/hr",
    val portfolioImages: List<String> = emptyList(),
    val isFollowing: Boolean = false
)

data class Measurements(
    val height: String,
    val bust: String,
    val waist: String,
    val hips: String
)

data class FeedPost(
    val id: String,
    val author: String,
    val location: String,
    val description: String,
    val imageUrl: String,
    val isStudioOrGear: Boolean = false,
    val likes: Int = 48,
    val isLiked: Boolean = false,
    val timestamp: String = "2h ago"
)

data class Hotspot(
    val id: String,
    val name: String,
    val location: String,
    val rating: Double,
    val creatorTip: String,
    val imageUrl: String,
    val checkInsCount: Int,
    val isCheckedIn: Boolean = false,
    val category: String = "BEST FOR PHOTOSHOOTS"
)

data class LeaderboardItem(
    val rank: Int,
    val province: String,
    val checkIns: String,
    val trend: TrendDirection
)

enum class TrendDirection {
    UP,
    STEADY,
    DOWN
}

data class ActiveDeal(
    val id: String,
    val brand: String,
    val campaign: String,
    val type: String,
    val status: String,
    val amount: String,
    val iconType: String
)

data class CollabRequest(
    val creatorName: String,
    val projectTitle: String,
    val roleRequired: String,
    val proposedDate: String,
    val budgetZar: String,
    val notes: String
)
