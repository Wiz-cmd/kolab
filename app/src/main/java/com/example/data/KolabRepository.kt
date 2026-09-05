package com.example.data

import android.content.Context
import com.example.data.security.ChatCrypto
import com.kolab.data.TokenManager
import com.kolab.data.UiMessage
import com.kolab.data.UiUser
import com.kolab.data.api.ApiClient
import com.kolab.data.api.ApiResult
import com.kolab.data.api.KolabApi
import com.kolab.data.api.dto.ChangePasswordRequest
import com.kolab.data.api.dto.CreateDealRequest
import com.kolab.data.api.dto.CreatePostRequest
import com.kolab.data.api.dto.DealResponse
import com.kolab.data.api.dto.DeviceTokenRequest
import com.kolab.data.api.dto.HotspotResponse
import com.kolab.data.api.dto.LoginRequest
import com.kolab.data.api.dto.PasswordResetRequest
import com.kolab.data.api.dto.PortfolioItemRequest
import com.kolab.data.api.dto.PostResponse
import com.kolab.data.api.dto.RefreshRequest
import com.kolab.data.api.dto.RegisterRequest
import com.kolab.data.api.dto.SocialExchangeRequest
import com.kolab.data.api.dto.SocialStartResponse
import com.kolab.data.api.dto.TwoFactorEnableRequest
import com.kolab.data.api.dto.TwoFactorSetupResponse
import com.kolab.data.api.dto.UpdateProfileRequest
import com.kolab.data.api.dto.UserResponse
import com.kolab.data.api.dto.WithdrawalRequest
import com.kolab.data.api.safeCall
import com.kolab.data.chat.ChatSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FeedMode { FOLLOWING, FOR_YOU }

class KolabRepository(
    private val context: Context,
    private val api: KolabApi,
    private val tokenManager: TokenManager,
    private val socketManager: ChatSocketManager
) {

    constructor() : this(AppGraph.applicationContext, ApiClient.getApi(), AppGraph.tokenManager, AppGraph.socketManager)

    constructor(context: Context) : this(context, ApiClient.getApi(), AppGraph.tokenManager, AppGraph.socketManager)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── UI-facing state ─────────────────────────────────────────────────
    private val _creatives = MutableStateFlow<List<CreativeProfile>>(emptyList())
    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    private val _hotspots = MutableStateFlow<List<Hotspot>>(emptyList())
    private val _leaderboard = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    private val _activeDeals = MutableStateFlow<List<ActiveDeal>>(emptyList())
    private val _availableBalance = MutableStateFlow(DEMO_BALANCE)
    private val _userPortfolio = MutableStateFlow<List<String>>(emptyList())
    private val _currentLocation = MutableStateFlow(DEFAULT_LOCATION)

    val creatives: StateFlow<List<CreativeProfile>> = _creatives.asStateFlow()
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()
    val hotspots: StateFlow<List<Hotspot>> = _hotspots.asStateFlow()
    val leaderboard: StateFlow<List<LeaderboardItem>> = _leaderboard.asStateFlow()
    val activeDeals: StateFlow<List<ActiveDeal>> = _activeDeals.asStateFlow()
    val availableBalance: StateFlow<Double> = _availableBalance.asStateFlow()
    val userPortfolio: StateFlow<List<String>> = _userPortfolio.asStateFlow()
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    // ── Auth ────────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun getUserName(): String = tokenManager.getUserName() ?: ""

    fun getUserEmail(): String = tokenManager.getUserEmail() ?: ""

    fun getCurrentUserId(): String = tokenManager.getUserId() ?: ""

    suspend fun login(email: String, password: String): Result<UiUser> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Success -> {
                tokenManager.saveTokens(r.data.accessToken, r.data.refreshToken)
                getMe()
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<UiUser> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.register(RegisterRequest(email.trim(), password, name)) }) {
            is ApiResult.Success -> {
                tokenManager.saveTokens(r.data.accessToken, r.data.refreshToken)
                getMe()
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun getMe(): Result<UiUser> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.getMe() }) {
            is ApiResult.Success -> {
                val u = r.data
                tokenManager.saveUserInfo(u.id, u.name, u.email, u.role, u.avatar)
                Result.success(
                    UiUser(
                        id = u.id,
                        name = u.name,
                        email = u.email,
                        role = u.role,
                        location = u.location ?: "",
                        avatar = u.avatar ?: "",
                        bio = u.bio ?: "",
                        isVerified = u.isVerified,
                        followersCount = u.followersCount,
                        followingCount = u.followingCount,
                        postsCount = u.postsCount
                    )
                )
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun logout() {
        tokenManager.getRefreshToken()?.let { rt ->
            withContext(Dispatchers.IO) { safeCall { api.logout(RefreshRequest(rt)) } }
        }
        tokenManager.clearAll()
        _activeDeals.value = emptyList()
    }

    // ── Loaders ─────────────────────────────────────────────────────────

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        coroutineScope {
            launch { loadCreatives() }
            launch { loadFeed(FeedMode.FOLLOWING) }
            launch { loadHotspots() }
            launch { loadLeaderboard() }
            launch { loadDeals() }
            launch { loadPortfolio() }
        }
    }

    private suspend fun loadCreatives() {
        when (val r = safeCall { api.listUsers(take = 50) }) {
            is ApiResult.Success -> {
                _creatives.value = r.data.data
                    .filter { !it.role.equals("BRAND", ignoreCase = true) }
                    .map { it.toCreative() }
            }
            else -> {}
        }
    }

    private suspend fun loadFeed() {
        when (val r = safeCall { api.getFeed() }) {
            is ApiResult.Success -> _feedPosts.value = r.data.data.map { it.toFeedPost() }
            else -> {}
        }
    }

    private suspend fun loadHotspots() {
        when (val r = safeCall { api.listHotspots() }) {
            is ApiResult.Success -> _hotspots.value = r.data.data.map { it.toHotspot() }
            else -> {}
        }
    }

    private suspend fun loadLeaderboard() {
        when (val r = safeCall { api.getLeaderboard() }) {
            is ApiResult.Success -> {
                val sorted = r.data.sortedByDescending { it.totalCheckIns }
                _leaderboard.value = sorted.take(10).mapIndexed { index, entry ->
                    LeaderboardItem(
                        rank = index + 1,
                        province = entry.province.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        checkIns = entry.totalCheckIns.toString(),
                        trend = when (index) {
                            0, 1 -> TrendDirection.UP
                            2 -> TrendDirection.STEADY
                            else -> TrendDirection.DOWN
                        }
                    )
                }
            }
            else -> {}
        }
    }

    private suspend fun loadDeals() {
        when (val r = safeCall { api.listDeals() }) {
            is ApiResult.Success -> {
                val me = tokenManager.getUserId()
                _activeDeals.value = r.data.data
                    .filter { me != null && (it.creator.id == me || it.brand.id == me) }
                    .map { it.toActiveDeal() }
            }
            else -> {}
        }
    }

    private suspend fun loadPortfolio() {
        when (val r = safeCall { api.listCollections() }) {
            is ApiResult.Success -> {
                val urls = r.data.flatMap { it.items }.map { it.url }.filter { it.isNotBlank() }
                if (urls.isNotEmpty()) _userPortfolio.value = urls
            }
            else -> {}
        }
    }

    // ── Actions ─────────────────────────────────────────────────────────

    fun toggleLike(postId: String) {
        val list = _feedPosts.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx == -1) return
        val post = list[idx]
        val newLiked = !post.isLiked
        list[idx] = post.copy(
            isLiked = newLiked,
            likes = (post.likes + if (newLiked) 1 else -1).coerceAtLeast(0)
        )
        _feedPosts.value = list

        scope.launch {
            when (val r = safeCall { api.toggleLike(postId) }) {
                is ApiResult.Success -> {
                    val reconciled = _feedPosts.value.toMutableList()
                    val i = reconciled.indexOfFirst { it.id == postId }
                    if (i != -1) {
                        reconciled[i] = reconciled[i].copy(isLiked = r.data.liked, likes = r.data.likesCount)
                    }
                    _feedPosts.value = reconciled
                }
                else -> {}
            }
        }
    }

    fun toggleCheckIn(hotspotId: String) {
        val list = _hotspots.value.toMutableList()
        val idx = list.indexOfFirst { it.id == hotspotId }
        if (idx == -1) return
        val spot = list[idx]
        list[idx] = spot.copy(isCheckedIn = true, checkInsCount = spot.checkInsCount + 1)
        _hotspots.value = list

        scope.launch {
            when (val r = safeCall { api.checkIn(hotspotId) }) {
                is ApiResult.Success -> {
                    val reconciled = _hotspots.value.toMutableList()
                    val i = reconciled.indexOfFirst { it.id == hotspotId }
                    if (i != -1) {
                        reconciled[i] = reconciled[i].copy(isCheckedIn = true, checkInsCount = r.data.checkInsCount)
                    }
                    _hotspots.value = reconciled
                }
                else -> {}
            }
        }
    }

    fun addPost(author: String, location: String, description: String, imageUrl: String, isStudio: Boolean) {
        scope.launch {
            when (val r = safeCall { api.createPost(CreatePostRequest(description = description, images = listOfNotNull(imageUrl.takeIf { it.isNotBlank() }), category = if (isStudio) "studio" else "general")) }) {
                is ApiResult.Success -> {
                    val post = r.data
                    _feedPosts.value = listOf(
                        FeedPost(
                            id = post.id,
                            author = post.author.name.ifBlank { author },
                            location = post.author.location ?: location,
                            description = description,
                            imageUrl = post.images.firstOrNull() ?: imageUrl,
                            isStudioOrGear = isStudio,
                            likes = post.likesCount,
                            isLiked = false,
                            timestamp = "just now"
                        )
                    ) + _feedPosts.value
                }
                else -> {}
            }
        }
    }

    fun addPortfolioImage(url: String) {
        if (url.isBlank()) return
        if (!_userPortfolio.value.contains(url)) {
            _userPortfolio.value = _userPortfolio.value + url
        }
        scope.launch {
            safeCall { api.addPortfolioItem(PortfolioItemRequest(title = "New project", url = url)) }
        }
    }

    suspend fun withdrawFunds(amount: Double): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        if (amount > _availableBalance.value) return@withContext false
        when (val r = safeCall { api.requestWithdrawal(WithdrawalRequest(amount, method = "bank")) }) {
            is ApiResult.Success -> {
                _availableBalance.value -= amount
                true
            }
            is ApiResult.Error -> false
            is ApiResult.Exception -> false
        }
    }

    fun setLocation(loc: String) {
        _currentLocation.value = loc
    }

    // ── Deals (collab request → deal) ───────────────────────────────────

    fun createDeal(creatorId: String, description: String, amount: Double) {
        scope.launch {
            when (val r = safeCall {
                api.createDeal(
                    CreateDealRequest(
                        creatorId = creatorId,
                        title = description.substringBefore('\n').take(100),
                        description = description,
                        amount = amount
                    )
                )
            }) {
                is ApiResult.Success -> loadDeals()
                else -> {}
            }
        }
    }

    // ── Chat ────────────────────────────────────────────────────────────

    suspend fun getMessages(dealId: String): Result<List<UiMessage>> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.getMessages(dealId) }) {
            is ApiResult.Success -> Result.success(
                r.data.data.map { msg ->
                    UiMessage(id = msg.id, content = msg.content, senderId = msg.senderId, createdAt = msg.createdAt)
                }
            )
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun registerDeviceToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.registerDeviceToken(DeviceTokenRequest(token)) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── Feed mode ───────────────────────────────────────────────────────

    suspend fun loadFeed(mode: FeedMode) {
        when (mode) {
            FeedMode.FOLLOWING -> {
                when (val r = safeCall { api.getFeed() }) {
                    is ApiResult.Success -> _feedPosts.value = r.data.data.map { it.toFeedPost() }
                    else -> {}
                }
            }
            FeedMode.FOR_YOU -> {
                when (val r = safeCall { api.getForYouFeed(take = 50) }) {
                    is ApiResult.Success -> _feedPosts.value = r.data.data.map { it.toFeedPost() }
                    else -> {}
                }
            }
        }
    }

    // ── Search ──────────────────────────────────────────────────────────

    private val searchPrefs = context.getSharedPreferences("kolab_search", Context.MODE_PRIVATE)

    fun recentSearches(): List<String> =
        searchPrefs.getString("recent", "")
            ?.split(SEARCH_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val updated = (listOf(q) + recentSearches().filter { !it.equals(q, ignoreCase = true) }).take(8)
        searchPrefs.edit().putString("recent", updated.joinToString(SEARCH_SEPARATOR)).apply()
    }

    fun clearRecentSearches() {
        searchPrefs.edit().remove("recent").apply()
    }

    suspend fun searchPosts(query: String, category: String? = null): Result<List<FeedPost>> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.getFeed(take = 30, search = query.ifBlank { null }, category = category) }) {
            is ApiResult.Success -> Result.success(r.data.data.map { it.toFeedPost() })
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun searchUsers(query: String): Result<List<CreativeProfile>> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.listUsers(take = 50, search = query.ifBlank { null }) }) {
            is ApiResult.Success -> Result.success(
                r.data.data.filter { !it.role.equals("BRAND", ignoreCase = true) }.map { it.toCreative() }
            )
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── Follow ──────────────────────────────────────────────────────────

    suspend fun followUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.followUser(userId) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun unfollowUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.unfollowUser(userId) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── E2EE chat key exchange ──────────────────────────────────────────

    fun chatCrypto(): ChatCrypto = ChatCrypto(context)

    /** Publishes this device's E2EE public key to the backend if it changed. */
    suspend fun ensurePublicKeyPublished(): String = withContext(Dispatchers.IO) {
        val pub = chatCrypto().myPublicKey()
        if (tokenManager.getPublicKey() != pub) {
            when (val r = safeCall { api.updateProfile(UpdateProfileRequest(publicKey = pub)) }) {
                is ApiResult.Success -> tokenManager.savePublicKey(pub)
                else -> {}
            }
        }
        pub
    }

    suspend fun getUserPublicKey(userId: String): String? = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.getUser(userId) }) {
            is ApiResult.Success -> r.data.publicKey?.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    /** The OTHER participant in a deal (the one that is not the current user). */
    suspend fun getDealPeerId(dealId: String): String? = withContext(Dispatchers.IO) {
        val me = tokenManager.getUserId() ?: return@withContext null
        when (val r = safeCall { api.getDeal(dealId) }) {
            is ApiResult.Success -> {
                val creator = r.data.creator.id
                val brand = r.data.brand.id
                when (me) {
                    creator -> brand
                    brand -> creator
                    else -> null
                }
            }
            else -> null
        }
    }

    // ── Account security ────────────────────────────────────────────────

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.changePassword(ChangePasswordRequest(currentPassword, newPassword)) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun setup2FA(): Result<TwoFactorSetupResponse> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.setup2FA() }) {
            is ApiResult.Success -> Result.success(r.data)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun enable2FA(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.enable2FA(TwoFactorEnableRequest(code)) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun disable2FA(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.disable2FA(TwoFactorEnableRequest(code)) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun logoutAllDevices(): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.logoutAll() }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── Social login ────────────────────────────────────────────────────

    suspend fun socialStart(provider: String): Result<SocialStartResponse> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.socialStart(provider) }) {
            is ApiResult.Success -> Result.success(r.data)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun socialExchange(provider: String, code: String, state: String): Result<UiUser> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.socialExchange(provider, SocialExchangeRequest(code, state)) }) {
            is ApiResult.Success -> {
                tokenManager.saveTokens(r.data.accessToken, r.data.refreshToken)
                getMe()
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── Mappers ─────────────────────────────────────────────────────────

    private fun UserResponse.toCreative() = CreativeProfile(
        id = id,
        name = name,
        role = prettifyRole(role),
        location = location ?: DEFAULT_LOCATION,
        category = prettifyRole(role),
        rate = "R2,500 / hr",
        rating = 4.9,
        imageUrl = avatar?.takeIf { it.isNotBlank() } ?: FALLBACK_AVATAR,
        followers = if (followersCount > 0) formatAbbrev(followersCount) else "14.2K",
        bio = bio ?: DEFAULT_BIO,
        collabsCount = 86
    )

    private fun PostResponse.toFeedPost() = FeedPost(
        id = id,
        author = author.name,
        location = author.location ?: "Joburg",
        description = description,
        imageUrl = images.firstOrNull() ?: "",
        isStudioOrGear = category.equals("studio", ignoreCase = true) || category.equals("gear", ignoreCase = true),
        likes = likesCount,
        isLiked = isLiked,
        timestamp = relativeTime(createdAt)
    )

    private fun HotspotResponse.toHotspot() = Hotspot(
        id = id,
        name = name,
        location = listOfNotNull(city, province).joinToString(" • "),
        rating = 4.8,
        creatorTip = description?.takeIf { it.isNotBlank() } ?: "Top creative-era hotspot in ${city.ifBlank { "South Africa" }}.",
        imageUrl = image ?: FALLBACK_HOTSPOT,
        checkInsCount = checkInsCount,
        isCheckedIn = false,
        category = category.uppercase()
    )

    private fun DealResponse.toActiveDeal() = ActiveDeal(
        id = id,
        brand = brand.name,
        campaign = description.substringBefore('\n').take(26),
        type = displayType(description),
        status = displayStatus(status),
        amount = "R${String.format(Locale.US, "%,.0f", amount)}",
        iconType = if (description.lowercase().contains("music") ||
            description.lowercase().contains("video") ||
            description.lowercase().contains("audio") ||
            description.lowercase().contains("song")
        ) "headphones" else "camera"
    )

    private fun prettifyRole(role: String): String =
        role.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

    private fun displayStatus(status: String): String = when (status.lowercase()) {
        "in_progress" -> "In Progress"
        "awaiting_payment" -> "Awaiting Payment"
        "payment_pending" -> "Payment Pending"
        "completed" -> "Completed"
        "cancelled" -> "Cancelled"
        "disputed" -> "Disputed"
        else -> status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun displayType(description: String): String {
        val lower = description.lowercase()
        return when {
            lower.contains("music") || lower.contains("song") || lower.contains("audio") -> "Music Video"
            lower.contains("video") || lower.contains("film") || lower.contains("commercial") -> "Video Shoot"
            lower.contains("fashion") || lower.contains("editorial") || lower.contains("runway") -> "Fashion Production"
            lower.contains("festival") || lower.contains("event") || lower.contains("tour") -> "Event Coverage"
            else -> "Creative Collaboration"
        }
    }

    private fun formatAbbrev(count: Int): String = when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }

    private fun relativeTime(iso: String): String {
        if (iso.isBlank()) return "just now"
        return try {
            val formats = arrayOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )
            var date: Date? = null
            for (format in formats) {
                format.isLenient = false
                try {
                    date = format.parse(iso)
                    if (date != null) break
                } catch (e: Exception) {
                    // try next format
                }
            }
            if (date == null) return iso
            val minutes = (System.currentTimeMillis() - date.time) / 60_000
            when {
                minutes < 1 -> "just now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 60 * 24 -> "${minutes / 60}h ago"
                minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d ago"
                else -> "${minutes / (60 * 24 * 7)}w ago"
            }
        } catch (e: Exception) {
            iso
        }
    }

    companion object {
        private const val DEFAULT_LOCATION = "Johannesburg, South Africa"
        private const val DEMO_BALANCE = 12450.0
        private const val DEFAULT_BIO = "Fashion Model & Creative Director based in South Africa. Working on editorial, high-concept, and commercial productions."
        private const val FALLBACK_AVATAR = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80"
        private const val FALLBACK_HOTSPOT = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&q=80"
        private const val SEARCH_SEPARATOR = "\u0001"
    }
}