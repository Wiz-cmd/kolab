package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.security.ChatCrypto
import com.kolab.data.TokenManager
import com.kolab.data.UiMessage
import com.kolab.data.UiUser
import com.kolab.data.api.ApiClient
import com.kolab.data.api.ApiResult
import com.kolab.data.api.KolabApi
import com.kolab.data.api.dto.ChallengeResponse
import com.kolab.data.api.dto.ChangePasswordRequest
import com.kolab.data.api.dto.ProfileViewersResponse
import com.kolab.data.api.dto.ProfileViewerResponse
import com.kolab.data.api.dto.CommentResponse
import com.kolab.data.api.dto.CreateCommentRequest
import com.kolab.data.api.dto.CreateDealRequest
import com.kolab.data.api.dto.CreatePostRequest
import com.kolab.data.api.dto.DealResponse
import com.kolab.data.api.dto.DeviceTokenRequest
import com.kolab.data.api.dto.HotspotResponse
import com.kolab.data.api.dto.LoginRequest
import com.kolab.data.api.dto.LoginOtpSettingsRequest
import com.kolab.data.api.dto.ResendLoginOtpRequest
import com.kolab.data.api.dto.VerifyLoginOtpRequest
import com.kolab.data.api.dto.PasswordResetRequest
import com.kolab.data.api.dto.PortfolioItemRequest
import com.kolab.data.api.dto.PostResponse
import com.kolab.data.api.dto.RefreshRequest
import com.kolab.data.api.dto.RegisterRequest
import com.kolab.data.api.dto.SocialExchangeRequest
import com.kolab.data.api.dto.SocialProviderResponse
import com.kolab.data.api.dto.SocialStartResponse
import com.kolab.data.api.dto.TwoFactorEnableRequest
import com.kolab.data.api.dto.TwoFactorSetupResponse
import com.kolab.data.api.dto.UpdatePostRequest
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val _availableBalance = MutableStateFlow(0.0)
    private val _userPortfolio = MutableStateFlow<List<String>>(emptyList())
    private val _currentLocation = MutableStateFlow(DEFAULT_LOCATION)
    private val _dataError = MutableStateFlow<String?>(null)
    private val _currentChallenge = MutableStateFlow<ChallengeResponse?>(null)
    private val _challengeLeaderboard = MutableStateFlow<List<FeedPost>>(emptyList())
    private val _risingCreators = MutableStateFlow<List<CreativeProfile>>(emptyList())
    private val _profileViewers = MutableStateFlow<List<ProfileViewerResponse>>(emptyList())
    private val _themeMode = MutableStateFlow(
        context.getSharedPreferences("kolab_prefs", Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, "system") ?: "system"
    )

    val creatives: StateFlow<List<CreativeProfile>> = _creatives.asStateFlow()
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()
    val hotspots: StateFlow<List<Hotspot>> = _hotspots.asStateFlow()
    val leaderboard: StateFlow<List<LeaderboardItem>> = _leaderboard.asStateFlow()
    val activeDeals: StateFlow<List<ActiveDeal>> = _activeDeals.asStateFlow()
    val availableBalance: StateFlow<Double> = _availableBalance.asStateFlow()
    val userPortfolio: StateFlow<List<String>> = _userPortfolio.asStateFlow()
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()
    val dataError: StateFlow<String?> = _dataError.asStateFlow()
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    val currentChallenge: StateFlow<ChallengeResponse?> = _currentChallenge.asStateFlow()
    val challengeLeaderboard: StateFlow<List<FeedPost>> = _challengeLeaderboard.asStateFlow()
    val risingCreators: StateFlow<List<CreativeProfile>> = _risingCreators.asStateFlow()
    val profileViewers: StateFlow<List<ProfileViewerResponse>> = _profileViewers.asStateFlow()

    fun setThemeMode(mode: String) {
        val value = if (mode in setOf("system", "light", "dark")) mode else "system"
        context.getSharedPreferences("kolab_prefs", Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_MODE, value).apply()
        _themeMode.value = value
    }

    // ── Guest session ───────────────────────────────────────────────────
    // Guests browse the live public API without tokens. Anything protected
    // returns 401 and the UI asks them to sign in.

    fun isGuestMode(): Boolean = tokenManager.isGuestMode()

    fun enterGuestSession() {
        tokenManager.enterGuestMode()
        _activeDeals.value = emptyList()
        _userPortfolio.value = emptyList()
        _availableBalance.value = 0.0
    }

    // ── Loading state (drives the shimmer skeletons) ────────────────────
    private val _creativesLoading = MutableStateFlow(false)
    private val _feedLoading = MutableStateFlow(false)
    private val _hotspotsLoading = MutableStateFlow(false)
    private val _portfolioLoading = MutableStateFlow(false)

    val creativesLoading: StateFlow<Boolean> = _creativesLoading.asStateFlow()
    val feedLoading: StateFlow<Boolean> = _feedLoading.asStateFlow()
    val hotspotsLoading: StateFlow<Boolean> = _hotspotsLoading.asStateFlow()
    val portfolioLoading: StateFlow<Boolean> = _portfolioLoading.asStateFlow()

    // ── Auth ────────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun getUserName(): String = tokenManager.getUserName() ?: ""

    fun getUserEmail(): String = tokenManager.getUserEmail() ?: ""

    fun getCurrentUserId(): String = tokenManager.getUserId() ?: ""

    suspend fun login(email: String, password: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Success -> {
                val body = r.data
                if (body.challenge) {
                    val id = body.challengeId
                    if (id.isNullOrBlank()) {
                        Result.failure(Exception("Login verification could not be started. Try again."))
                    } else {
                        Result.success(
                            LoginResult.Challenge(
                                challengeId = id,
                                channel = body.channel ?: "email",
                                destination = body.destination ?: "your account",
                                expiresIn = body.expiresIn,
                                devCode = body.devCode
                            )
                        )
                    }
                } else {
                    val access = body.accessToken
                    val refresh = body.refreshToken
                    if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                        Result.failure(Exception("Sign-in did not return valid tokens."))
                    } else {
                        tokenManager.saveTokens(access, refresh)
                        getMe().map { LoginResult.SignedIn(it) }
                    }
                }
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    /** Complete a password + OTP sign-in. */
    suspend fun verifyLoginOtp(challengeId: String, code: String): Result<UiUser> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.verifyLoginOtp(VerifyLoginOtpRequest(challengeId, code)) }) {
            is ApiResult.Success -> {
                tokenManager.saveTokens(r.data.accessToken, r.data.refreshToken)
                getMe()
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun resendLoginOtp(challengeId: String): Result<String?> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.resendLoginOtp(ResendLoginOtpRequest(challengeId)) }) {
            is ApiResult.Success -> Result.success(r.data.devCode)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    /** Update marketplace discovery preferences for the signed-in user. */
    suspend fun setDiscoveryPrefs(
        availableForCollab: Boolean? = null,
        showProfileViews: Boolean? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall {
            api.updateProfile(
                UpdateProfileRequest(
                    availableForCollab = availableForCollab,
                    showProfileViews = showProfileViews
                )
            )
        }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun setLoginOtp(enabled: Boolean, channel: String, phone: String?): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall {
            api.setLoginOtp(LoginOtpSettingsRequest(enabled, channel, phone?.ifBlank { null }))
        }) {
            is ApiResult.Success -> Result.success(Unit)
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

    suspend fun requestPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext Result.failure(Exception("Enter your email address first"))
        when (val r = safeCall { api.requestPasswordReset(PasswordResetRequest(email.trim())) }) {
            is ApiResult.Success -> Result.success(Unit)
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

    /**
     * Permanently deletes the signed-in account (Play Store requirement).
     * Local credentials are always cleared, even if the network call fails, so
     * a deleted session can never be resumed from this device.
     */
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to delete your account."))
        }
        when (val r = safeCall { api.deleteAccount() }) {
            is ApiResult.Success -> {
                logout()
                Result.success(Unit)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun logout() {
        if (tokenManager.isLoggedIn()) {
            tokenManager.getRefreshToken()?.let { rt ->
                withContext(Dispatchers.IO) { safeCall { api.logout(RefreshRequest(rt)) } }
            }
        }
        tokenManager.clearAll()
        _activeDeals.value = emptyList()
        _creatives.value = emptyList()
        _feedPosts.value = emptyList()
        _hotspots.value = emptyList()
        _leaderboard.value = emptyList()
        _userPortfolio.value = emptyList()
        _availableBalance.value = 0.0
    }

    // ── Loaders ─────────────────────────────────────────────────────────

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        _dataError.value = null
        coroutineScope {
            launch { loadCreatives() }
            launch { loadFeed(FeedMode.FOLLOWING) }
            launch { loadHotspots() }
            launch { loadLeaderboard() }
            if (tokenManager.isLoggedIn()) {
                launch { loadDeals() }
                launch { loadPortfolio() }
                launch { loadProfileViewers() }
            }
            launch { loadDiscovery() }
        }
    }

    private suspend fun loadDiscovery() {
        when (val r = safeCall { api.currentChallenge() }) {
            is ApiResult.Success -> _currentChallenge.value = r.data
            else -> {}
        }
        when (val r = safeCall { api.challengeLeaderboard(limit = 10) }) {
            is ApiResult.Success -> _challengeLeaderboard.value = r.data.data.map { it.toFeedPost() }
            else -> {}
        }
        val city = _currentLocation.value.split(',')[0].trim()
        when (val r = safeCall { api.risingCreators(city = city, take = 12) }) {
            is ApiResult.Success -> _risingCreators.value = r.data.map { it.toCreative() }
            else -> {}
        }
    }

    private suspend fun loadProfileViewers() {
        when (val r = safeCall { api.profileViewers() }) {
            is ApiResult.Success -> _profileViewers.value = r.data.viewers
            else -> {}
        }
    }

    private suspend fun loadCreatives() {
        _creativesLoading.value = true
        try {
            when (val r = safeCall { api.listUsers(take = 50) }) {
                is ApiResult.Success -> {
                    _creatives.value = r.data.data
                        .filter { !it.role.equals("BRAND", ignoreCase = true) }
                        .map { it.toCreative() }
                }
                else -> noteLoadFailure()
            }
        } finally {
            _creativesLoading.value = false
        }
    }

    /** The city portion of the selected location, used for geo-relevant feeds. */
    private fun feedLocation(): String? =
        _currentLocation.value.split(',')[0].trim().ifBlank { null }

    private suspend fun loadFeed() {
        _feedLoading.value = true
        try {
            when (val r = safeCall { api.getFeed(location = feedLocation()) }) {
                is ApiResult.Success -> _feedPosts.value = r.data.data.map { it.toFeedPost() }
                else -> noteLoadFailure()
            }
        } finally {
            _feedLoading.value = false
        }
    }

    private suspend fun loadHotspots() {
        _hotspotsLoading.value = true
        try {
            when (val r = safeCall { api.listHotspots() }) {
                is ApiResult.Success -> _hotspots.value = r.data.data.map { it.toHotspot() }
                else -> noteLoadFailure()
            }
        } finally {
            _hotspotsLoading.value = false
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
            else -> noteLoadFailure()
        }
    }

    private suspend fun loadDeals() {
        if (!tokenManager.isLoggedIn()) {
            _activeDeals.value = emptyList()
            return
        }
        when (val r = safeCall { api.listDeals() }) {
            is ApiResult.Success -> {
                val me = tokenManager.getUserId()
                _activeDeals.value = r.data.data
                    .filter { me != null && (it.creator.id == me || it.brand.id == me) }
                    .map { it.toActiveDeal() }
            }
            else -> noteLoadFailure()
        }
    }

    private suspend fun loadPortfolio() {
        if (!tokenManager.isLoggedIn()) {
            _userPortfolio.value = emptyList()
            return
        }
        _portfolioLoading.value = true
        try {
            when (val r = safeCall { api.listCollections() }) {
                is ApiResult.Success -> {
                    val urls = r.data.flatMap { it.items }.map { it.url }.filter { it.isNotBlank() }
                    if (urls.isNotEmpty()) _userPortfolio.value = urls
                }
                else -> noteLoadFailure()
            }
        } finally {
            _portfolioLoading.value = false
        }
    }

    private fun noteLoadFailure() {
        _dataError.value = "Can't reach Kolab. Check your connection and try again."
    }

    /** Let the UI hide the connection banner without changing loaded data. */
    fun dismissDataError() {
        _dataError.value = null
    }

    // ── Actions ─────────────────────────────────────────────────────────

    fun toggleLike(postId: String) {
        if (!tokenManager.isLoggedIn()) return
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
        if (!tokenManager.isLoggedIn()) return
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

    fun addPost(
        author: String,
        location: String,
        description: String,
        imageUrl: String,
        isStudio: Boolean,
        videoUrl: String = "",
        filter: String = "clean"
    ) {
        if (!tokenManager.isLoggedIn()) return
        scope.launch {
            when (val r = safeCall {
                api.createPost(
                    CreatePostRequest(
                        description = description,
                        imageUrl = imageUrl.takeIf { it.isNotBlank() },
                        videoUrl = videoUrl.takeIf { it.isNotBlank() },
                        filter = filter.takeIf { it.isNotBlank() },
                        location = location.takeIf { it.isNotBlank() },
                        category = if (isStudio) "studio" else "general"
                    )
                )
            }) {
                is ApiResult.Success -> {
                    val post = r.data
                    _feedPosts.value = listOf(post.toFeedPost()) + _feedPosts.value
                }
                else -> {}
            }
        }
    }

    /** Creates a post and prepends it to the live feed on success. */
    suspend fun createPost(
        author: String,
        location: String,
        description: String,
        imageUrl: String,
        isStudio: Boolean,
        videoUrl: String = "",
        filter: String = "clean"
    ): Result<FeedPost> = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to publish a Frame."))
        }
        when (val r = safeCall {
            api.createPost(
                CreatePostRequest(
                    description = description,
                    imageUrl = imageUrl.takeIf { it.isNotBlank() },
                    videoUrl = videoUrl.takeIf { it.isNotBlank() },
                    filter = filter.takeIf { it.isNotBlank() },
                    location = location.takeIf { it.isNotBlank() },
                    category = if (isStudio) "studio" else "general"
                )
            )
        }) {
            is ApiResult.Success -> {
                val post = r.data.toFeedPost()
                _feedPosts.value = listOf(post) + _feedPosts.value
                Result.success(post)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    /** Edit an existing Frame's caption and metadata. */
    suspend fun updatePost(
        postId: String,
        description: String,
        location: String
    ): Result<FeedPost> = withContext(Dispatchers.IO) {
        when (val r = safeCall {
            api.updatePost(
                postId,
                UpdatePostRequest(
                    description = description.trim(),
                    location = location.trim().ifBlank { null }
                )
            )
        }) {
            is ApiResult.Success -> {
                val updated = r.data.toFeedPost()
                _feedPosts.value = _feedPosts.value.map { if (it.id == postId) updated else it }
                Result.success(updated)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    /** Delete one of the signed-in user's Frames. */
    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.deletePost(postId) }) {
            is ApiResult.Success -> {
                _feedPosts.value = _feedPosts.value.filterNot { it.id == postId }
                Result.success(Unit)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    /**
     * Uploads a photo or short video chosen/captured on the device.
     * Returns the absolute URL to save on the post.
     */
    suspend fun uploadMedia(uri: Uri, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to share a Frame."))
        }
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Could not read that file."))
            val isVideo = mimeType.startsWith("video/")
            val max = if (isVideo) MAX_VIDEO_BYTES else MAX_IMAGE_BYTES
            if (bytes.size > max) {
                val mb = max / (1024 * 1024)
                return@withContext Result.failure(Exception("That file is too large. Keep it under ${mb}MB."))
            }
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("gif") -> "gif"
                isVideo -> "mp4"
                else -> "jpg"
            }
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "file",
                "frame-${System.currentTimeMillis()}.$extension",
                body
            )
            when (val r = safeCall { api.uploadFile(part) }) {
                is ApiResult.Success -> Result.success(absoluteMediaUrl(r.data.url))
                is ApiResult.Error -> Result.failure(Exception(r.message))
                is ApiResult.Exception -> Result.failure(r.throwable)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed. Check your connection and try again."))
        }
    }

    /** Records a Frame view. Guests are allowed and counted too. */
    fun recordView(postId: String) {
        val list = _feedPosts.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx == -1) return
        list[idx] = list[idx].copy(views = list[idx].views + 1)
        _feedPosts.value = list

        scope.launch { safeCall { api.recordView(postId) } }
    }

    suspend fun loadComments(postId: String): Result<List<FeedComment>> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.getComments(postId) }) {
            is ApiResult.Success -> {
                val me = tokenManager.getUserId()
                Result.success(r.data.data.map { it.toFeedComment(postId, me) })
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun addComment(postId: String, content: String): Result<FeedComment> = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to comment."))
        }
        when (val r = safeCall { api.createComment(postId, CreateCommentRequest(content.trim())) }) {
            is ApiResult.Success -> {
                val me = tokenManager.getUserId()
                val comment = r.data.toFeedComment(postId, me)
                bumpCommentCount(postId, 1)
                Result.success(comment)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.deleteComment(postId, commentId) }) {
            is ApiResult.Success -> {
                bumpCommentCount(postId, -1)
                Result.success(Unit)
            }
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    private fun bumpCommentCount(postId: String, delta: Int) {
        val list = _feedPosts.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx == -1) return
        list[idx] = list[idx].copy(comments = (list[idx].comments + delta).coerceAtLeast(0))
        _feedPosts.value = list
    }

    fun addPortfolioImage(url: String) {
        if (url.isBlank() || !tokenManager.isLoggedIn()) return
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
        if (!tokenManager.isLoggedIn()) return@withContext false
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
        if (!tokenManager.isLoggedIn()) return
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
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to view messages."))
        }
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
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to enable notifications."))
        }
        when (val r = safeCall { api.registerDeviceToken(DeviceTokenRequest(token)) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    // ── Feed mode ───────────────────────────────────────────────────────

    suspend fun loadFeed(mode: FeedMode) {
        _feedLoading.value = true
        try {
            when (mode) {
                FeedMode.FOLLOWING -> {
                    when (val r = safeCall { api.getFeed(location = feedLocation()) }) {
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
        } finally {
            _feedLoading.value = false
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
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to follow creatives."))
        }
        when (val r = safeCall { api.followUser(userId) }) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

    suspend fun unfollowUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) {
            return@withContext Result.failure(Exception("Sign in to manage who you follow."))
        }
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
        if (!tokenManager.isLoggedIn()) return@withContext pub
        if (tokenManager.getPublicKey() != pub) {
            when (val r = safeCall { api.updateProfile(UpdateProfileRequest(publicKey = pub)) }) {
                is ApiResult.Success -> tokenManager.savePublicKey(pub)
                else -> {}
            }
        }
        pub
    }

    suspend fun getUserPublicKey(userId: String): String? = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) return@withContext null
        when (val r = safeCall { api.getUser(userId) }) {
            is ApiResult.Success -> r.data.publicKey?.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    /** The OTHER participant in a deal (the one that is not the current user). */
    suspend fun getDealPeerId(dealId: String): String? = withContext(Dispatchers.IO) {
        if (!tokenManager.isLoggedIn()) return@withContext null
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

    /** Canonical provider keys the server can actually authenticate. */
    suspend fun socialProviders(): Result<List<String>> = withContext(Dispatchers.IO) {
        when (val r = safeCall { api.socialProviders() }) {
            is ApiResult.Success -> Result.success(
                r.data.filter { it.configured }.map { it.provider.lowercase() }
            )
            is ApiResult.Error -> Result.failure(Exception(r.message))
            is ApiResult.Exception -> Result.failure(r.throwable)
        }
    }

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

    private fun absoluteMediaUrl(raw: String?): String {
        val url = raw.orEmpty()
        if (url.isBlank()) return ""
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        val origin = com.example.BuildConfig.BASE_URL.substringBefore("/api/").trimEnd('/')
        return "$origin/${url.trimStart('/')}"
    }

    private fun CommentResponse.toFeedComment(postId: String, me: String?) = FeedComment(
        id = id,
        postId = postId,
        authorId = author.id,
        authorName = author.name,
        authorAvatar = absoluteMediaUrl(author.avatar),
        content = content,
        timestamp = relativeTime(createdAt),
        isMine = me != null && author.id == me
    )

    private fun UserResponse.toCreative() = CreativeProfile(
        id = id,
        name = name,
        role = prettifyRole(role),
        location = location?.takeIf { it.isNotBlank() } ?: DEFAULT_LOCATION,
        category = prettifyRole(role),
        rate = "Rate on request",
        rating = 0.0,
        imageUrl = avatar.orEmpty(),
        followers = formatAbbrev(followersCount),
        bio = bio?.takeIf { it.isNotBlank() } ?: "",
        collabsCount = postsCount,
        availableForCollab = availableForCollab,
        availabilityStatus = if (availableForCollab) "Open to collab" else "Booked"
    )

    private fun PostResponse.toFeedPost() = FeedPost(
        id = id,
        author = author.name.ifBlank { "Kolab Creator" },
        location = location ?: author.location.orEmpty(),
        description = description,
        imageUrl = absoluteMediaUrl(imageUrl),
        isStudioOrGear = category.equals("studio", ignoreCase = true) || category.equals("gear", ignoreCase = true),
        likes = likesCount,
        isLiked = isLiked,
        timestamp = relativeTime(createdAt),
        authorId = author.id,
        authorAvatar = absoluteMediaUrl(author.avatar),
        videoUrl = absoluteMediaUrl(videoUrl),
        filter = filter?.takeIf { it.isNotBlank() } ?: "clean",
        views = viewsCount,
        comments = commentsCount,
        createdAtIso = createdAt
    )

    private fun HotspotResponse.toHotspot() = Hotspot(
        id = id,
        name = name,
        location = listOfNotNull(city, province).joinToString(" • "),
        rating = 0.0,
        creatorTip = description?.takeIf { it.isNotBlank() } ?: "Creative hotspot in ${city.ifBlank { "South Africa" }}.",
        imageUrl = image.orEmpty(),
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
        private const val KEY_THEME_MODE = "theme_mode"
        private const val SEARCH_SEPARATOR = "\u0001"
        private const val MAX_IMAGE_BYTES = 15 * 1024 * 1024L
        private const val MAX_VIDEO_BYTES = 50 * 1024 * 1024L
    }
}