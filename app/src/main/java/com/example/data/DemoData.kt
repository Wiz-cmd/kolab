package com.example.data

/**
 * Seeded, fully-local content for Kolab's offline "demo mode".
 *
 * When no backend is reachable the app falls back to this dataset so every
 * screen (Home, Discover, Places, Profile, chat entry points) renders with
 * believable South-African creator content instead of dead empty states.
 *
 * Images are stable Unsplash CDN URLs, matching the rest of the app so Coil
 * caching works the same way.
 */
object DemoData {

    const val CURRENT_USER_ID = "demo-creator-001"
    const val CURRENT_USER_NAME = "Zanele Ndlovu"
    const val CURRENT_USER_EMAIL = "studio@kolab.com"

    private const val P1 = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&q=80"
    private const val P2 = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&q=80"
    private const val P3 = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=800&q=80"
    private const val P4 = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=800&q=80"
    private const val P5 = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&q=80"
    private const val P6 = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=800&q=80"
    private const val P7 = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=800&q=80"
    private const val P8 = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=800&q=80"

    private const val F1 = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1000&q=80"
    private const val F2 = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1000&q=80"
    private const val F3 = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1000&q=80"
    private const val F4 = "https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1000&q=80"
    private const val F5 = "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=1000&q=80"
    private const val F6 = "https://images.unsplash.com/photo-1479064555552-3ef4979f8908?w=1000&q=80"

    private const val H1 = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&q=80"
    private const val H2 = "https://images.unsplash.com/photo-1521017432531-fbd92d768814?w=800&q=80"
    private const val H3 = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80"
    private const val H4 = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&q=80"
    private const val H5 = "https://images.unsplash.com/photo-1470337458703-46ad1756a187?w=800&q=80"
    private const val H6 = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80"

    fun creatives(): List<CreativeProfile> = listOf(
        CreativeProfile(
            id = "demo-c-01", name = "Zanele Ndlovu", role = "Model",
            location = "Johannesburg, Gauteng", category = "Model", rate = "R2,500 / hr",
            rating = 4.9, reviewCount = 42, imageUrl = P1, followers = "14.2K", collabsCount = 86,
            bio = "Fashion model & creative director. Editorial, high-concept and commercial productions.",
            startingRateHourly = "Starting at R2,500/hr"
        ),
        CreativeProfile(
            id = "demo-c-02", name = "Thabo Mokoena", role = "Photographer",
            location = "Cape Town, Western Cape", category = "Photographer", rate = "R3,200 / hr",
            rating = 4.8, reviewCount = 31, imageUrl = P4, followers = "9.7K", collabsCount = 54,
            bio = "Editorial & campaign photographer shooting on film and digital across Cape Town.",
            startingRateHourly = "Starting at R3,200/hr"
        ),
        CreativeProfile(
            id = "demo-c-03", name = "Aisha Patel", role = "Videographer",
            location = "Durban, KwaZulu-Natal", category = "Videographer", rate = "R4,000 / day",
            rating = 5.0, reviewCount = 18, imageUrl = P2, followers = "6.1K", collabsCount = 27,
            bio = "Music video & brand film director. Amapiano visuals a speciality.",
            startingRateHourly = "Starting at R4,000/day"
        ),
        CreativeProfile(
            id = "demo-c-04", name = "Lwazi Dlamini", role = "3D Artist",
            location = "Sandton, Gauteng", category = "3D Artist", rate = "R3,800 / hr",
            rating = 4.7, reviewCount = 22, imageUrl = P7, followers = "5.4K", collabsCount = 33,
            bio = "CGI product renders, motion design and virtual set builds for brands.",
            measurements = Measurements("—", "—", "—", "—")
        ),
        CreativeProfile(
            id = "demo-c-05", name = "Naledi Khumalo", role = "Stylist",
            location = "Pretoria, Gauteng", category = "Stylist", rate = "R1,900 / hr",
            rating = 4.9, reviewCount = 27, imageUrl = P3, followers = "8.3K", collabsCount = 61,
            bio = "Wardrobe styling for editorial, runway and music videos. Thrift-to-luxury.",
            measurements = Measurements("—", "—", "—", "—")
        ),
        CreativeProfile(
            id = "demo-c-06", name = "Kagiso Sithole", role = "Photographer",
            location = "Soweto, Gauteng", category = "Photographer", rate = "R2,100 / hr",
            rating = 4.6, reviewCount = 15, imageUrl = P8, followers = "4.2K", collabsCount = 19,
            bio = "Street, documentary and portrait work rooted in Soweto culture.",
            startingRateHourly = "Starting at R2,100/hr"
        ),
        CreativeProfile(
            id = "demo-c-07", name = "Chloé van Wyk", role = "Model",
            location = "Stellenbosch, Western Cape", category = "Model", rate = "R2,800 / hr",
            rating = 4.8, reviewCount = 24, imageUrl = P6, followers = "11.5K", collabsCount = 48,
            bio = "Runway and beauty model. Commercial, bridal and lifestyle campaigns.",
            startingRateHourly = "Starting at R2,800/hr"
        ),
        CreativeProfile(
            id = "demo-c-08", name = "Sipho Nkosi", role = "Videographer",
            location = "Port Elizabeth, Eastern Cape", category = "Videographer", rate = "R2,900 / day",
            rating = 4.7, reviewCount = 12, imageUrl = P5, followers = "3.8K", collabsCount = 14,
            bio = "Event, festival and tour coverage with same-week turnaround edits.",
            startingRateHourly = "Starting at R2,900/day"
        )
    )

    fun feedPosts(): List<FeedPost> = listOf(
        FeedPost(
            id = "demo-p-01", author = "Zanele Ndlovu", location = "Johannesburg",
            description = "Golden hour on the rooftop — new editorial series shot with @thabo.mokoena. Bookings open for October.",
            imageUrl = F1, likes = 342, timestamp = "2h ago"
        ),
        FeedPost(
            id = "demo-p-02", author = "Thabo Mokoena", location = "Cape Town",
            description = "Behind the scenes from the summer campaign. Natural light only, no retouch. 🎞️",
            imageUrl = F2, likes = 217, timestamp = "5h ago"
        ),
        FeedPost(
            id = "demo-p-03", author = "Studio Nine Randburg", location = "Randburg",
            description = "Our new cyc wall is live. Day rates available — perfect for lookbooks and product shoots.",
            imageUrl = F3, isStudioOrGear = true, likes = 128, timestamp = "Yesterday"
        ),
        FeedPost(
            id = "demo-p-04", author = "Aisha Patel", location = "Durban",
            description = "Amapiano video dropping Friday. Shot on the Durban beachfront at sunrise. 🎬",
            imageUrl = F4, likes = 501, timestamp = "Yesterday"
        ),
        FeedPost(
            id = "demo-p-05", author = "Naledi Khumalo", location = "Pretoria",
            description = "Thrifted this whole look for under R400. Styling is not about budget, it's about eye.",
            imageUrl = F5, likes = 289, timestamp = "2d ago"
        ),
        FeedPost(
            id = "demo-p-06", author = "Kagiso Sithole", location = "Soweto",
            description = "Faces of Vilakazi Street — a portrait series celebrating local makers.",
            imageUrl = F6, likes = 176, timestamp = "3d ago"
        )
    )

    fun hotspots(): List<Hotspot> = listOf(
        Hotspot(
            id = "demo-h-01", name = "Maboneng Precinct", location = "Johannesburg • Gauteng",
            rating = 4.9, creatorTip = "Best rooftop light between 16:30–18:00. Permits needed for commercial crews.",
            imageUrl = H1, checkInsCount = 412, category = "BEST FOR STREET"
        ),
        Hotspot(
            id = "demo-h-02", name = "V&A Waterfront", location = "Cape Town • Western Cape",
            rating = 4.7, creatorTip = "Table Mountain backdrops at golden hour. Arrive early — it gets busy.",
            imageUrl = H2, checkInsCount = 388, category = "BEST FOR CAMPAIGNS"
        ),
        Hotspot(
            id = "demo-h-03", name = "Umhlanga Rocks Pier", location = "Durban • KwaZulu-Natal",
            rating = 4.8, creatorTip = "Sunrise ocean shots. Watch the tide — tripods get wet.",
            imageUrl = H3, checkInsCount = 274, category = "BEST FOR VIDEO"
        ),
        Hotspot(
            id = "demo-h-04", name = "Union Buildings Gardens", location = "Pretoria • Gauteng",
            rating = 4.6, creatorTip = "Formal architecture for editorial. Quietest weekday mornings.",
            imageUrl = H4, checkInsCount = 190, category = "BEST FOR EDITORIAL"
        ),
        Hotspot(
            id = "demo-h-05", name = "Vilakazi Street", location = "Soweto • Gauteng",
            rating = 4.9, creatorTip = "Rich street culture and murals. Great for documentary portraits.",
            imageUrl = H5, checkInsCount = 356, category = "BEST FOR PORTRAITS"
        ),
        Hotspot(
            id = "demo-h-06", name = "Camps Bay Tidal Pool", location = "Cape Town • Western Cape",
            rating = 4.8, creatorTip = "Turquoise water at midday. Bring reflectors for swimwear shoots.",
            imageUrl = H6, checkInsCount = 302, category = "BEST FOR SWIMWEAR"
        )
    )

    fun leaderboard(): List<LeaderboardItem> = listOf(
        LeaderboardItem(1, "Gauteng", "1284", TrendDirection.UP),
        LeaderboardItem(2, "Western Cape", "1102", TrendDirection.UP),
        LeaderboardItem(3, "KwaZulu-Natal", "876", TrendDirection.STEADY),
        LeaderboardItem(4, "Eastern Cape", "512", TrendDirection.DOWN),
        LeaderboardItem(5, "Free State", "348", TrendDirection.UP),
        LeaderboardItem(6, "Mpumalanga", "291", TrendDirection.STEADY)
    )

    fun activeDeals(): List<ActiveDeal> = listOf(
        ActiveDeal(
            id = "demo-d-01", brand = "Aurora Swimwear", campaign = "Summer capsule campaign",
            type = "Fashion Production", status = "In Progress", amount = "R18,500", iconType = "camera"
        ),
        ActiveDeal(
            id = "demo-d-02", brand = "Amapiano Sessions", campaign = "Music video feature",
            type = "Music Video", status = "Delivered", amount = "R12,000", iconType = "headphones"
        ),
        ActiveDeal(
            id = "demo-d-03", brand = "Kasi Coffee Co.", campaign = "Lifestyle brand shoot",
            type = "Creative Collaboration", status = "Awaiting Payment", amount = "R7,400", iconType = "camera"
        )
    )

    fun portfolio(): List<String> = listOf(P1, F1, P3, F2, P5, F4, P7, F5, P8)
}
