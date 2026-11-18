package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveDeal
import com.example.data.AppGraph
import com.example.data.CollabRequest
import com.example.data.CreativeProfile
import com.example.data.FeedComment
import com.example.data.FeedMode
import com.example.data.FeedPost
import com.example.data.LoginResult
import com.example.data.Hotspot
import com.example.data.KolabRepository
import com.example.data.LeaderboardItem
import com.kolab.data.api.dto.SocialStartResponse
import com.kolab.data.api.dto.TwoFactorSetupResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class SearchResults(
    val posts: List<FeedPost> = emptyList(),
    val users: List<CreativeProfile> = emptyList(),
    val isLoading: Boolean = false,
    val searchedQuery: String = ""
)

enum class AuthStep {
    LOGIN,
    SIGN_UP,
    LOGIN_OTP,
    GOOGLE_AUTH_SHEET,
    APPLE_AUTH_SHEET,
    INSTAGRAM_AUTH_SHEET,
    X_AUTH_SHEET,
    SOCIAL_CALLBACK,
    ACCOUNT_CREATED_SUCCESS,
    AUTHENTICATED
}

enum class NavigationTab {
    HOME,
    DISCOVER,
    PLACES,
    CREATE,
    PROFILE
}

enum class ProfileSubTab {
    MY_STUDIO,
    PUBLIC_PROFILE
}

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String
)

/** A captured or picked photo/video waiting for a caption and publishing. */
data class FrameDraftMedia(
    val uri: Uri,
    val mimeType: String,
    val lookKey: String
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
}

class KolabViewModel(
    private val repository: KolabRepository = AppGraph.repository
) : ViewModel() {

    // Auth State
    private val _authStep = MutableStateFlow(AuthStep.LOGIN)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(repository.getUserEmail())
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow(repository.getUserName())
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginChallenge = MutableStateFlow<LoginResult.Challenge?>(null)
    val loginChallenge: StateFlow<LoginResult.Challenge?> = _loginChallenge.asStateFlow()

    /** Canonical social providers configured on the backend (google/twitter/instagram). */
    private val _availableSocialProviders = MutableStateFlow<Set<String>>(emptySet())
    val availableSocialProviders: StateFlow<Set<String>> = _availableSocialProviders.asStateFlow()

    /** True while browsing the public API without an account. */
    private val _isGuestMode = MutableStateFlow(repository.isGuestMode() && !repository.isLoggedIn())
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    // Navigation State
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _profileSubTab = MutableStateFlow(ProfileSubTab.MY_STUDIO)
    val profileSubTab: StateFlow<ProfileSubTab> = _profileSubTab.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCreativeCategory = MutableStateFlow("All Creatives")
    val selectedCreativeCategory: StateFlow<String> = _selectedCreativeCategory.asStateFlow()

    private val _selectedHotspotCategory = MutableStateFlow("ALL")
    val selectedHotspotCategory: StateFlow<String> = _selectedHotspotCategory.asStateFlow()

    // Search
    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    // Feed mode
    private val _feedMode = MutableStateFlow(FeedMode.FOLLOWING)
    val feedMode: StateFlow<FeedMode> = _feedMode.asStateFlow()

    // Following set (for follow buttons on discover/search results)
    private val _followingIds = MutableStateFlow<Set<String>>(emptySet())
    val followingIds: StateFlow<Set<String>> = _followingIds.asStateFlow()

    // Security
    private val _securityBusy = MutableStateFlow(false)
    val securityBusy: StateFlow<Boolean> = _securityBusy.asStateFlow()

    // Wallet
    private val _withdrawBusy = MutableStateFlow(false)
    val withdrawBusy: StateFlow<Boolean> = _withdrawBusy.asStateFlow()

    // Dialogs & Sheets
    private val _collabTargetCreative = MutableStateFlow<CreativeProfile?>(null)
    val collabTargetCreative: StateFlow<CreativeProfile?> = _collabTargetCreative.asStateFlow()

    private val _showCreateCollabSheet = MutableStateFlow(false)
    val showCreateCollabSheet: StateFlow<Boolean> = _showCreateCollabSheet.asStateFlow()

    private val _showWithdrawDialog = MutableStateFlow(false)
    val showWithdrawDialog: StateFlow<Boolean> = _showWithdrawDialog.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // ── Frames (photo/video posts) ────────────────────────────────────
    private val _showFrameCamera = MutableStateFlow(false)
    val showFrameCamera: StateFlow<Boolean> = _showFrameCamera.asStateFlow()

    private val _draftMedia = MutableStateFlow<FrameDraftMedia?>(null)
    val draftMedia: StateFlow<FrameDraftMedia?> = _draftMedia.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _framesViewerPostId = MutableStateFlow<String?>(null)
    val framesViewerPostId: StateFlow<String?> = _framesViewerPostId.asStateFlow()

    private val _commentsPost = MutableStateFlow<FeedPost?>(null)
    val commentsPost: StateFlow<FeedPost?> = _commentsPost.asStateFlow()

    private val _comments = MutableStateFlow<List<FeedComment>>(emptyList())
    val comments: StateFlow<List<FeedComment>> = _comments.asStateFlow()

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    private val _editingPost = MutableStateFlow<FeedPost?>(null)
    val editingPost: StateFlow<FeedPost?> = _editingPost.asStateFlow()

    private val _commentBusy = MutableStateFlow(false)
    val commentBusy: StateFlow<Boolean> = _commentBusy.asStateFlow()

    // Repository Flows
    val feedPosts: StateFlow<List<FeedPost>> = repository.feedPosts
    val hotspots: StateFlow<List<Hotspot>> = repository.hotspots
    val leaderboard: StateFlow<List<LeaderboardItem>> = repository.leaderboard
    val activeDeals: StateFlow<List<ActiveDeal>> = repository.activeDeals
    val availableBalance: StateFlow<Double> = repository.availableBalance
    val userPortfolio: StateFlow<List<String>> = repository.userPortfolio
    val currentLocation: StateFlow<String> = repository.currentLocation
    val dataError: StateFlow<String?> = repository.dataError
    val themeMode: StateFlow<String> = repository.themeMode

    // Loading state for the shimmer skeletons
    val feedLoading: StateFlow<Boolean> = repository.feedLoading
    val creativesLoading: StateFlow<Boolean> = repository.creativesLoading
    val hotspotsLoading: StateFlow<Boolean> = repository.hotspotsLoading
    val portfolioLoading: StateFlow<Boolean> = repository.portfolioLoading

    // Filtered Creatives
    val filteredCreatives: StateFlow<List<CreativeProfile>> = combine(
        repository.creatives,
        _searchQuery,
        _selectedCreativeCategory
    ) { list, query, category ->
        list.filter { creative ->
            val matchesCategory = if (category == "All Creatives") true else {
                creative.category.equals(category, ignoreCase = true) ||
                        creative.role.contains(category, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                    creative.name.contains(query, ignoreCase = true) ||
                    creative.role.contains(query, ignoreCase = true) ||
                    creative.location.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        restoreSession()
        // Discover which social sign-ins are actually available so the login
        // screen never offers a button that cannot connect.
        viewModelScope.launch {
            repository.socialProviders()
                .onSuccess { _availableSocialProviders.value = it.toSet() }
        }
    }

    private fun restoreSession() {
        if (repository.isGuestMode() && !repository.isLoggedIn()) {
            _isGuestMode.value = true
            _currentUserName.value = "Guest"
            _currentUserEmail.value = ""
            _authStep.value = AuthStep.AUTHENTICATED
            loadHomeData()
            return
        }
        if (!repository.isLoggedIn()) return
        viewModelScope.launch {
            repository.getMe()
                .onSuccess { user ->
                    _isGuestMode.value = false
                    _currentUserName.value = user.name.ifBlank { "Kolab Creator" }
                    _currentUserEmail.value = user.email
                    _authStep.value = AuthStep.AUTHENTICATED
                    loadHomeData()
                }
                .onFailure {
                    // Keep a cached signed-in session usable when the network is
                    // briefly unavailable; only fall back to login if the
                    // credentials are actually gone.
                    val cachedName = repository.getUserName()
                    if (repository.isLoggedIn() && cachedName.isNotBlank()) {
                        _currentUserName.value = cachedName
                        _currentUserEmail.value = repository.getUserEmail()
                        _authStep.value = AuthStep.AUTHENTICATED
                        loadHomeData()
                    } else {
                        _authStep.value = AuthStep.LOGIN
                    }
                }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch { repository.refreshAll() }
    }

    /** Retries the public data load after a connection failure. */
    fun retryLoad() {
        viewModelScope.launch { repository.refreshAll() }
    }

    /** Hides the connection banner; the next refresh can show it again if needed. */
    fun dismissDataError() = repository.dismissDataError()

    // Auth actions
    fun setAuthStep(step: AuthStep) {
        _authStep.value = step
    }

    /** Enters guest browsing without creating an account. */
    fun continueAsGuest() {
        if (_isLoggingIn.value) return
        repository.enterGuestSession()
        _isGuestMode.value = true
        _currentUserName.value = "Guest"
        _currentUserEmail.value = ""
        _authStep.value = AuthStep.AUTHENTICATED
        _currentTab.value = NavigationTab.HOME
        loadHomeData()
        showNotification("Browsing as guest. Sign in when you're ready to book or collaborate.")
    }

    private fun requireAccount(action: String): Boolean {
        if (!repository.isLoggedIn()) {
            showNotification("Sign in to $action")
            return false
        }
        return true
    }

    fun login(email: String, pass: String) {
        if (_isLoggingIn.value) return
        if (email.isBlank() || pass.isBlank()) {
            showNotification("Please enter your email and password")
            return
        }
        _isLoggingIn.value = true
        viewModelScope.launch {
            try {
                repository.login(email, pass)
                    .onSuccess { result ->
                        when (result) {
                            is LoginResult.Challenge -> {
                                _loginChallenge.value = result
                                _authStep.value = AuthStep.LOGIN_OTP
                            }
                            is LoginResult.SignedIn -> {
                                _isGuestMode.value = false
                                _currentUserName.value = result.user.name.ifBlank { "Kolab Creator" }
                                _currentUserEmail.value = result.user.email
                                _authStep.value = AuthStep.AUTHENTICATED
                                loadHomeData()
                                showNotification("Welcome back to Kolab")
                            }
                        }
                    }
                    .onFailure { e ->
                        val msg = when {
                            e.message.isNullOrBlank() -> "Unable to sign in. Check your connection and try again."
                            e.message.orEmpty().contains("credentials", ignoreCase = true) -> "Incorrect email or password"
                            else -> e.message!!
                        }
                        showNotification(msg)
                    }
            } finally {
                _isLoggingIn.value = false
            }
        }
    }

    /** Confirm the emailed/texted code and finish signing in. */
    fun verifyLoginOtp(code: String) {
        val challenge = _loginChallenge.value ?: return
        if (code.length != 6 || !code.all { it.isDigit() }) {
            showNotification("Enter the 6-digit code we sent you")
            return
        }
        _isLoggingIn.value = true
        viewModelScope.launch {
            try {
                repository.verifyLoginOtp(challenge.challengeId, code)
                    .onSuccess { user ->
                        _loginChallenge.value = null
                        _isGuestMode.value = false
                        _currentUserName.value = user.name.ifBlank { "Kolab Creator" }
                        _currentUserEmail.value = user.email
                        _authStep.value = AuthStep.AUTHENTICATED
                        loadHomeData()
                        showNotification("Identity verified — welcome back")
                    }
                    .onFailure { showNotification(it.message ?: "That code didn't work. Try again.") }
            } finally {
                _isLoggingIn.value = false
            }
        }
    }

    fun resendLoginOtp() {
        val challenge = _loginChallenge.value ?: return
        viewModelScope.launch {
            repository.resendLoginOtp(challenge.challengeId)
                .onSuccess {
                    if (it != null) showNotification("New code: $it")
                    else showNotification("A new code is on its way")
                }
                .onFailure { showNotification(it.message ?: "Couldn't resend the code") }
        }
    }

    fun cancelLoginOtp() {
        _loginChallenge.value = null
        _authStep.value = AuthStep.LOGIN
    }

    fun signUp(name: String, email: String, pass: String) {
        if (_isLoggingIn.value) return
        if (email.isBlank() || pass.isBlank()) {
            showNotification("Please fill in all the details")
            return
        }
        _isLoggingIn.value = true
        viewModelScope.launch {
            try {
                repository.register(name.ifBlank { "Kolab Creator" }, email, pass)
                    .onSuccess { user ->
                        _isGuestMode.value = false
                        _currentUserName.value = user.name.ifBlank { name.ifBlank { "Kolab Creator" } }
                        _currentUserEmail.value = user.email
                        _authStep.value = AuthStep.ACCOUNT_CREATED_SUCCESS
                        loadHomeData()
                    }
                    .onFailure { e ->
                        showNotification(e.message ?: "Unable to create your account")
                    }
            } finally {
                _isLoggingIn.value = false
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            showNotification("Enter your email address first")
            return
        }
        viewModelScope.launch {
            repository.requestPasswordReset(email)
                .onSuccess { showNotification("Password reset instructions sent to your email") }
                .onFailure { showNotification(it.message ?: "Unable to send reset instructions") }
        }
    }

    fun completeSocialSignIn(provider: String, onResult: (Result<SocialStartResponse>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.socialStart(provider))
        }
    }

    fun completeSocial(provider: String, code: String, state: String) {
        viewModelScope.launch {
            repository.socialExchange(provider, code, state)
                .onSuccess { user ->
                    _isGuestMode.value = false
                    _currentUserName.value = user.name.ifBlank { "Kolab Creator" }
                    _currentUserEmail.value = user.email
                    _authStep.value = AuthStep.AUTHENTICATED
                    loadHomeData()
                    showNotification("Signed in with $provider")
                }
                .onFailure {
                    showNotification(it.message ?: "Unable to complete $provider sign-in")
                    _authStep.value = AuthStep.LOGIN
                }
        }
    }

    fun setLoginOtp(
        enabled: Boolean,
        channel: String,
        phone: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.setLoginOtp(enabled, channel, phone)
            result.onSuccess {
                showNotification(
                    if (enabled) "Login verification enabled" else "Login verification disabled"
                )
            }
            result.onFailure { showNotification(it.message ?: "Couldn't update login verification") }
            onResult(result)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) {
        _securityBusy.value = true
        viewModelScope.launch {
            val result = repository.changePassword(currentPassword, newPassword)
            _securityBusy.value = false
            onResult(result)
        }
    }

    fun setup2FA(onResult: (Result<TwoFactorSetupResponse>) -> Unit) {
        _securityBusy.value = true
        viewModelScope.launch {
            val result = repository.setup2FA()
            _securityBusy.value = false
            onResult(result)
        }
    }

    fun enable2FA(code: String, onResult: (Result<Unit>) -> Unit) {
        _securityBusy.value = true
        viewModelScope.launch {
            val result = repository.enable2FA(code)
            _securityBusy.value = false
            onResult(result)
        }
    }

    fun disable2FA(code: String, onResult: (Result<Unit>) -> Unit) {
        _securityBusy.value = true
        viewModelScope.launch {
            val result = repository.disable2FA(code)
            _securityBusy.value = false
            onResult(result)
        }
    }

    fun logoutAllDevices(onResult: (Result<Unit>) -> Unit) {
        _securityBusy.value = true
        viewModelScope.launch {
            val result = repository.logoutAllDevices()
            _securityBusy.value = false
            onResult(result)
        }
    }

    fun signOut() {
        viewModelScope.launch { repository.logout() }
        _isGuestMode.value = false
        _currentUserName.value = ""
        _currentUserEmail.value = ""
        _authStep.value = AuthStep.LOGIN
        _currentTab.value = NavigationTab.HOME
    }

    /**
     * Deletes the account on the server, then returns to the login screen.
     * Runs through the repository so tokens and cached state are cleared too.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            val result = repository.deleteAccount()
            result.onFailure {
                showNotification(it.message ?: "Could not delete your account. Please try again.")
            }
            if (result.isSuccess) {
                _isGuestMode.value = false
                _currentUserName.value = ""
                _currentUserEmail.value = ""
                _authStep.value = AuthStep.LOGIN
                _currentTab.value = NavigationTab.HOME
                showNotification("Your account has been deleted.")
            }
        }
    }

    // Tab Navigation
    fun selectTab(tab: NavigationTab) {
        if (tab == NavigationTab.CREATE) {
            if (!requireAccount("create a collaboration")) return
            _showCreateCollabSheet.value = true
        } else {
            _currentTab.value = tab
        }
    }

    fun setProfileSubTab(subTab: ProfileSubTab) {
        _profileSubTab.value = subTab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _searchResults.value = SearchResults()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            val q = trimmed
            _searchResults.value = SearchResults(isLoading = true, searchedQuery = q)
            val users = repository.searchUsers(q)
                .getOrDefault(emptyList())
                .sortedWith(compareByDescending { rankUser(it, q) })
            val posts = repository.searchPosts(q, searchCategoryToApi(_selectedCreativeCategory.value))
                .getOrDefault(emptyList())
            repository.addRecentSearch(q)
            _searchResults.value = SearchResults(
                posts = posts,
                users = users,
                isLoading = false,
                searchedQuery = q
            )
        }
    }

    fun recentSearches(): List<String> = repository.recentSearches()

    fun clearRecentSearches() {
        repository.clearRecentSearches()
    }

    fun rankUser(user: CreativeProfile, query: String): Int {
        val q = query.lowercase()
        val name = user.name.lowercase()
        val score = when {
            name == q -> 100
            name.startsWith(q) -> 80
            name.split(" ").any { it.startsWith(q) } -> 60
            name.contains(q) -> 40
            else -> 0
        }
        var bonus = 0
        if (user.role.lowercase().contains(q)) bonus += 10
        if (user.category.lowercase().contains(q)) bonus += 8
        if (user.location.lowercase().contains(q)) bonus += 5
        if (user.bio.lowercase().contains(q)) bonus += 3
        return score + bonus
    }

    private fun searchCategoryToApi(category: String): String? = when (category.lowercase()) {
        "all creatives", "all", "" -> null
        else -> category.lowercase().replace("s", "")
    }

    fun setFeedMode(mode: FeedMode) {
        if (_feedMode.value == mode) return
        _feedMode.value = mode
        viewModelScope.launch { repository.loadFeed(mode) }
    }

    fun toggleFollow(creativeId: String, name: String) {
        if (!requireAccount("follow creatives")) return
        val currentlyFollowing = _followingIds.value.contains(creativeId)
        val shouldFollow = !currentlyFollowing
        _followingIds.update { if (shouldFollow) it + creativeId else it - creativeId }
        viewModelScope.launch {
            val result = if (shouldFollow) {
                repository.followUser(creativeId)
            } else {
                repository.unfollowUser(creativeId)
            }
            result
                .onSuccess {
                    showNotification(if (shouldFollow) "Following $name" else "Unfollowed $name")
                }
                .onFailure {
                    _followingIds.update { ids -> if (shouldFollow) ids - creativeId else ids + creativeId }
                    showNotification("Unable to update follow status")
                }
        }
    }

    fun setCreativeCategory(category: String) {
        _selectedCreativeCategory.value = category
    }

    fun setHotspotCategory(category: String) {
        _selectedHotspotCategory.value = category
    }

    fun toggleLike(postId: String) {
        if (!requireAccount("like posts")) return
        repository.toggleLike(postId)
    }

    fun toggleCheckIn(hotspotId: String) {
        if (!requireAccount("check in at hotspots")) return
        repository.toggleCheckIn(hotspotId)
        showNotification("You're in! Check-in updated.")
    }

    fun openCollabRequest(creative: CreativeProfile) {
        if (!requireAccount("send collaboration requests")) return
        _collabTargetCreative.value = creative
    }

    fun closeCollabRequest() {
        _collabTargetCreative.value = null
    }

    fun openCreateSheet() {
        if (!requireAccount("create a post or collaboration")) return
        _showCreateCollabSheet.value = true
    }

    fun closeCreateSheet() {
        _showCreateCollabSheet.value = false
    }

    // ── Frames actions ────────────────────────────────────────────────

    fun openFrameCamera() {
        if (!requireAccount("share a Frame")) return
        _showCreateCollabSheet.value = false
        _showFrameCamera.value = true
    }

    fun closeFrameCamera() {
        _showFrameCamera.value = false
    }

    /** Called by the camera after a capture or gallery pick. */
    fun onFrameMediaReady(uri: Uri, mimeType: String, lookKey: String) {
        _showFrameCamera.value = false
        _draftMedia.value = FrameDraftMedia(uri = uri, mimeType = mimeType, lookKey = lookKey)
    }

    fun discardFrameDraft() {
        if (_isPublishing.value) return
        _draftMedia.value = null
    }

    fun openFramesViewer(postId: String? = null) {
        _framesViewerPostId.value = postId ?: ""
    }

    fun closeFramesViewer() {
        _framesViewerPostId.value = null
    }

    fun recordFrameView(postId: String) {
        repository.recordView(postId)
    }

    fun openComments(post: FeedPost) {
        _commentsPost.value = post
        _comments.value = emptyList()
        _commentsLoading.value = true
        viewModelScope.launch {
            repository.loadComments(post.id)
                .onSuccess { _comments.value = it }
                .onFailure { showNotification(it.message ?: "Could not load comments") }
            _commentsLoading.value = false
        }
    }

    fun closeComments() {
        if (_commentBusy.value) return
        _commentsPost.value = null
        _comments.value = emptyList()
    }

    fun submitComment(text: String) {
        val post = _commentsPost.value ?: return
        if (_commentBusy.value) return
        if (!requireAccount("comment on a Frame")) return
        val content = text.trim()
        if (content.isBlank()) return
        _commentBusy.value = true
        viewModelScope.launch {
            try {
                repository.addComment(post.id, content)
                    .onSuccess { comment ->
                        _comments.value = listOf(comment) + _comments.value
                        val current = _commentsPost.value ?: post
                        _commentsPost.value = current.copy(comments = current.comments + 1)
                    }
                    .onFailure {
                        showNotification(it.message ?: "Could not post that comment")
                    }
            } finally {
                _commentBusy.value = false
            }
        }
    }

    fun removeComment(comment: FeedComment) {
        val post = _commentsPost.value ?: return
        if (_commentBusy.value) return
        _commentBusy.value = true
        viewModelScope.launch {
            try {
                repository.deleteComment(post.id, comment.id)
                    .onSuccess {
                        _comments.value = _comments.value.filterNot { it.id == comment.id }
                        val current = _commentsPost.value ?: post
                        _commentsPost.value = current.copy(comments = (current.comments - 1).coerceAtLeast(0))
                    }
                    .onFailure {
                        showNotification(it.message ?: "Could not delete that comment")
                    }
            } finally {
                _commentBusy.value = false
            }
        }
    }

    // ── Manage your own Frames ─────────────────────────────────────────

    private fun ownsPost(post: FeedPost): Boolean =
        post.authorId.isNotBlank() && post.authorId == repository.getCurrentUserId()

    fun openEditPost(post: FeedPost) {
        if (!requireAccount("edit your Frames")) return
        if (!ownsPost(post)) {
            showNotification("You can only edit your own Frames")
            return
        }
        _editingPost.value = post
    }

    fun closeEditPost() {
        _editingPost.value = null
    }

    fun saveEditedPost(description: String, location: String) {
        val post = _editingPost.value ?: return
        viewModelScope.launch {
            repository.updatePost(post.id, description, location)
                .onSuccess {
                    showNotification("Frame updated")
                    _editingPost.value = null
                }
                .onFailure { showNotification(it.message ?: "Could not update that Frame") }
        }
    }

    fun deleteOwnPost(post: FeedPost) {
        if (!requireAccount("delete your Frames")) return
        if (!ownsPost(post)) {
            showNotification("You can only delete your own Frames")
            return
        }
        viewModelScope.launch {
            repository.deletePost(post.id)
                .onSuccess { showNotification("Frame deleted") }
                .onFailure { showNotification(it.message ?: "Could not delete that Frame") }
        }
    }

    /**
     * Uploads the selected media, creates the Frame, and closes the
     * composer. Upload can take a while on mobile data, so the composer stays
     * open with a progress state until it finishes.
     */
    fun publishFrame(
        description: String,
        location: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val draft = _draftMedia.value ?: return
        if (_isPublishing.value) return
        if (!requireAccount("share a Frame")) return
        _isPublishing.value = true
        viewModelScope.launch {
            try {
                repository.uploadMedia(draft.uri, draft.mimeType)
                    .onSuccess { url ->
                        val caption = description.trim().ifBlank {
                            if (draft.isVideo) "A moment worth sharing." else "A little of life, right now."
                        }
                        repository.createPost(
                            author = _currentUserName.value,
                            location = location.trim(),
                            description = caption,
                            imageUrl = if (draft.isVideo) "" else url,
                            isStudio = false,
                            videoUrl = if (draft.isVideo) url else "",
                            filter = draft.lookKey
                        )
                            .onSuccess {
                                _draftMedia.value = null
                                _showCreateCollabSheet.value = false
                                _currentTab.value = NavigationTab.HOME
                                showNotification("Frame shared")
                                onResult(true)
                            }
                            .onFailure {
                                showNotification(it.message ?: "Could not publish your Frame")
                                onResult(false)
                            }
                    }
                    .onFailure {
                        showNotification(it.message ?: "Upload failed. Try again.")
                        onResult(false)
                    }
            } finally {
                _isPublishing.value = false
            }
        }
    }

    fun openWithdrawDialog() {
        if (!requireAccount("withdraw funds")) return
        _showWithdrawDialog.value = true
    }

    fun closeWithdrawDialog() {
        _showWithdrawDialog.value = false
    }

    fun submitCollabRequest(request: CollabRequest) {
        if (!requireAccount("send collaboration requests")) return
        _collabTargetCreative.value?.let { target ->
            val amount = request.budgetZar
                .replace("R", "")
                .replace(",", "")
                .replace(" ", "")
                .toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                showNotification("Enter a budget for this collaboration")
                return
            }
            repository.createDeal(
                creatorId = target.id,
                description = listOfNotNull(
                    request.projectTitle,
                    request.roleRequired,
                    if (request.proposedDate.isNotBlank()) "Proposed date: ${request.proposedDate}" else null
                ).distinct().joinToString("\n"),
                amount = amount
            )
        }
        _collabTargetCreative.value = null
        showNotification("Collaboration request sent to ${request.creatorName}")
    }

    fun createNewPost(author: String, location: String, description: String, imageUrl: String, isStudio: Boolean) {
        if (!requireAccount("publish a post")) return
        repository.addPost(author, location, description, imageUrl, isStudio)
        _showCreateCollabSheet.value = false
        _currentTab.value = NavigationTab.HOME
        showNotification("Post published to Joburg creative feed")
    }

    fun addPortfolioImage(url: String) {
        if (!requireAccount("add portfolio work")) return
        repository.addPortfolioImage(url)
        showNotification("New project added to portfolio grid")
    }

    fun withdrawFunds(amount: Double) {
        if (!requireAccount("withdraw funds")) return
        if (_withdrawBusy.value) return
        _withdrawBusy.value = true
        viewModelScope.launch {
            try {
                val success = repository.withdrawFunds(amount)
                _showWithdrawDialog.value = false
                if (success) {
                    showNotification("R${String.format(Locale.US, "%,.2f", amount)} transferred to designated bank account")
                } else {
                    showNotification("Withdrawal failed. Check your balance and try again.")
                }
            } finally {
                _withdrawBusy.value = false
            }
        }
    }

    fun switchLocation(loc: String) {
        repository.setLocation(loc)
        // Refresh the feed so it immediately reflects the new local area.
        viewModelScope.launch { repository.loadFeed(_feedMode.value) }
        showNotification("Showing Frames near $loc")
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
        showNotification(
            when (mode) {
                "light" -> "Light theme enabled"
                "dark" -> "Dark theme enabled"
                else -> "Following system theme"
            }
        )
    }

    fun showNotification(msg: String) {
        _notification.value = UiNotification(message = msg)
    }

    fun dismissNotification() {
        _notification.value = null
    }
}