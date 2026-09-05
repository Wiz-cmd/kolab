package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveDeal
import com.example.data.AppGraph
import com.example.data.CollabRequest
import com.example.data.CreativeProfile
import com.example.data.FeedMode
import com.example.data.FeedPost
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

class KolabViewModel(
    private val repository: KolabRepository = AppGraph.repository
) : ViewModel() {

    // Auth State
    private val _authStep = MutableStateFlow(AuthStep.LOGIN)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(repository.getUserEmail().ifBlank { "studio@kolab.com" })
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow(repository.getUserName().ifBlank { "Zanele Ndlovu" })
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

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

    private val _isMapViewEnabled = MutableStateFlow(false)
    val isMapViewEnabled: StateFlow<Boolean> = _isMapViewEnabled.asStateFlow()

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

    // Dialogs & Sheets
    private val _collabTargetCreative = MutableStateFlow<CreativeProfile?>(null)
    val collabTargetCreative: StateFlow<CreativeProfile?> = _collabTargetCreative.asStateFlow()

    private val _showCreateCollabSheet = MutableStateFlow(false)
    val showCreateCollabSheet: StateFlow<Boolean> = _showCreateCollabSheet.asStateFlow()

    private val _showWithdrawDialog = MutableStateFlow(false)
    val showWithdrawDialog: StateFlow<Boolean> = _showWithdrawDialog.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Repository Flows
    val feedPosts: StateFlow<List<FeedPost>> = repository.feedPosts
    val hotspots: StateFlow<List<Hotspot>> = repository.hotspots
    val leaderboard: StateFlow<List<LeaderboardItem>> = repository.leaderboard
    val activeDeals: StateFlow<List<ActiveDeal>> = repository.activeDeals
    val availableBalance: StateFlow<Double> = repository.availableBalance
    val userPortfolio: StateFlow<List<String>> = repository.userPortfolio
    val currentLocation: StateFlow<String> = repository.currentLocation

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
    }

    private fun restoreSession() {
        if (!repository.isLoggedIn()) return
        viewModelScope.launch {
            repository.getMe()
                .onSuccess { user ->
                    _currentUserName.value = user.name.ifBlank { "Kolab Creator" }
                    _currentUserEmail.value = user.email
                    _authStep.value = AuthStep.AUTHENTICATED
                    loadHomeData()
                }
                .onFailure {
                    _authStep.value = AuthStep.LOGIN
                }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch { repository.refreshAll() }
    }

    // Auth actions
    fun setAuthStep(step: AuthStep) {
        _authStep.value = step
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            showNotification("Please enter your email and password")
            return
        }
        viewModelScope.launch {
            repository.login(email, pass)
                .onSuccess { user ->
                    _currentUserName.value = user.name.ifBlank { "Kolab Creator" }
                    _currentUserEmail.value = user.email
                    _authStep.value = AuthStep.AUTHENTICATED
                    loadHomeData()
                    showNotification("Welcome back to Kolab")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message.isNullOrBlank() -> "Unable to sign in. Check your connection and try again."
                        e.message.orEmpty().contains("credentials", ignoreCase = true) -> "Incorrect email or password"
                        else -> e.message!!
                    }
                    showNotification(msg)
                }
        }
    }

    fun signUp(name: String, email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            showNotification("Please fill in all the details")
            return
        }
        viewModelScope.launch {
            repository.register(name.ifBlank { "Kolab Creator" }, email, pass)
                .onSuccess { user ->
                    _currentUserName.value = user.name.ifBlank { name.ifBlank { "Kolab Creator" } }
                    _currentUserEmail.value = user.email
                    _authStep.value = AuthStep.ACCOUNT_CREATED_SUCCESS
                    loadHomeData()
                }
                .onFailure { e ->
                    showNotification(e.message ?: "Unable to create your account")
                }
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
        _authStep.value = AuthStep.LOGIN
        _currentTab.value = NavigationTab.HOME
    }

    // Tab Navigation
    fun selectTab(tab: NavigationTab) {
        if (tab == NavigationTab.CREATE) {
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

    fun toggleMapView() {
        _isMapViewEnabled.value = !_isMapViewEnabled.value
    }

    fun toggleLike(postId: String) {
        repository.toggleLike(postId)
    }

    fun toggleCheckIn(hotspotId: String) {
        repository.toggleCheckIn(hotspotId)
        showNotification("You're in! Check-in updated.")
    }

    fun openCollabRequest(creative: CreativeProfile) {
        _collabTargetCreative.value = creative
    }

    fun closeCollabRequest() {
        _collabTargetCreative.value = null
    }

    fun openCreateSheet() {
        _showCreateCollabSheet.value = true
    }

    fun closeCreateSheet() {
        _showCreateCollabSheet.value = false
    }

    fun openWithdrawDialog() {
        _showWithdrawDialog.value = true
    }

    fun closeWithdrawDialog() {
        _showWithdrawDialog.value = false
    }

    fun submitCollabRequest(request: CollabRequest) {
        _collabTargetCreative.value?.let { target ->
            val amount = request.budgetZar
                .replace("R", "")
                .replace(",", "")
                .replace(" ", "")
                .toDoubleOrNull() ?: 0.0
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
        repository.addPost(author, location, description, imageUrl, isStudio)
        _showCreateCollabSheet.value = false
        _currentTab.value = NavigationTab.HOME
        showNotification("Post published to Joburg creative feed")
    }

    fun addPortfolioImage(url: String) {
        repository.addPortfolioImage(url)
        showNotification("New project added to portfolio grid")
    }

    fun withdrawFunds(amount: Double) {
        viewModelScope.launch {
            val success = repository.withdrawFunds(amount)
            _showWithdrawDialog.value = false
            if (success) {
                showNotification("R${String.format(Locale.US, "%,.2f", amount)} transferred to designated bank account")
            } else {
                showNotification("Withdrawal failed. Check your balance and try again.")
            }
        }
    }

    fun switchLocation(loc: String) {
        repository.setLocation(loc)
        showNotification("Switched location to $loc")
    }

    fun showNotification(msg: String) {
        _notification.value = UiNotification(message = msg)
    }

    fun dismissNotification() {
        _notification.value = null
    }
}