package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.fragment.app.FragmentActivity
import com.example.data.AppGraph
import com.example.data.security.AppLockManager
import com.example.data.security.BiometricAuth
import com.example.ui.AuthStep
import com.example.ui.KolabViewModel
import com.example.ui.NavigationTab
import com.example.ui.auth.AccountCreatedScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.auth.SocialAuthScreen
import com.example.ui.components.CreateCollabSheet
import com.example.ui.components.KolabBottomBar
import com.example.ui.discover.DiscoverScreen
import com.example.ui.home.HomeScreen
import com.example.ui.places.PlacesScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.KolabTheme
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextSecondary
import com.kolab.ui.chat.ChatScreen
import com.kolab.ui.chat.ChatViewModel
import com.kolab.ui.chat.ChatViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SocialCallback(
    val provider: String,
    val code: String,
    val state: String
)

class MainActivity : FragmentActivity() {

    private val socialCallbackFlow = MutableStateFlow<SocialCallback?>(null)
    val socialCallback: kotlinx.coroutines.flow.StateFlow<SocialCallback?> = socialCallbackFlow.asStateFlow()

    private val appLockFlow = MutableStateFlow(false)
    val appLock: kotlinx.coroutines.flow.StateFlow<Boolean> = appLockFlow.asStateFlow()

    private var appLockManager: AppLockManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appLockManager = AppLockManager(this)
        if (appLockManager?.isEnabled() == true) {
            appLockFlow.value = true
        }
        parseSocialIntent(intent)?.let { socialCallbackFlow.value = it }
        val initialDealId = intent.getStringExtra("dealId")
        setContent {
            KolabTheme {
                KolabApp(
                    initialDealId = initialDealId,
                    socialCallback = socialCallback.collectAsState().value,
                    appLocked = appLock.collectAsState().value,
                    onRequestUnlock = { promptForUnlock() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (appLockFlow.value) {
            // Let the UI settle before showing the OS prompt.
            Handler(Looper.getMainLooper()).postDelayed({ promptForUnlock() }, 250)
        }
    }

    override fun onStop() {
        super.onStop()
        if (appLockManager?.isEnabled() == true) {
            appLockFlow.value = true
        }
    }

    private fun promptForUnlock() {
        if (!appLockFlow.value) return
        BiometricAuth.authenticate(
            this,
            title = "Kolab",
            subtitle = "Authenticate to unlock Kolab",
            onSuccess = { appLockFlow.value = false },
            onError = { _, _ -> }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseSocialIntent(intent)?.let { socialCallbackFlow.value = it }
    }

    private fun parseSocialIntent(intent: Intent): SocialCallback? {
        val uri = intent.data ?: return null
        if (uri.scheme != "kolab" || uri.host != "auth") return null
        val provider = uri.getQueryParameter("provider") ?: return null
        val code = uri.getQueryParameter("code") ?: return null
        val state = uri.getQueryParameter("state") ?: return null
        if (provider.isBlank() || code.isBlank() || state.isBlank()) return null
        return SocialCallback(provider, code, state)
    }

    companion object {
        fun openAuthUrl(context: android.content.Context, url: String) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        }
    }
}

@Composable
fun KolabApp(
    viewModel: KolabViewModel = viewModel(),
    initialDealId: String? = null,
    socialCallback: SocialCallback? = null,
    appLocked: Boolean = false,
    onRequestUnlock: () -> Unit = {}
) {
    val authStep by viewModel.authStep.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val showCreateSheet by viewModel.showCreateCollabSheet.collectAsState()
    val notification by viewModel.notification.collectAsState()

    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(AppGraph.repository, AppGraph.socketManager)
    )
    var activeChatDeal by remember { mutableStateOf<String?>(initialDealId) }
    BackHandler(enabled = activeChatDeal != null) { activeChatDeal = null }

    // Notification toast timer
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3500)
            viewModel.dismissNotification()
        }
    }

    // Social login deep link (kolab://auth/callback?provider=...&code=...&state=...)
    androidx.compose.runtime.LaunchedEffect(socialCallback) {
        socialCallback?.let { cb ->
            viewModel.setAuthStep(AuthStep.SOCIAL_CALLBACK)
            viewModel.completeSocial(cb.provider, cb.code, cb.state)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlassBackground(modifier = Modifier.fillMaxSize())

        AnimatedContent(
            targetState = authStep,
            transitionSpec = {
                when {
                    targetState == AuthStep.SIGN_UP && initialState == AuthStep.LOGIN -> {
                        (slideInHorizontally(tween(350)) { it } + fadeIn(tween(350)))
                            .togetherWith(slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(350)))
                    }
                    targetState == AuthStep.LOGIN && initialState == AuthStep.SIGN_UP -> {
                        (slideInHorizontally(tween(350)) { -it } + fadeIn(tween(350)))
                            .togetherWith(slideOutHorizontally(tween(350)) { it } + fadeOut(tween(350)))
                    }
                    targetState == AuthStep.ACCOUNT_CREATED_SUCCESS -> {
                        (slideInVertically(tween(400)) { it } + fadeIn(tween(400)))
                            .togetherWith(fadeOut(tween(250)))
                    }
                    targetState == AuthStep.AUTHENTICATED -> {
                        (fadeIn(tween(450)) + slideInVertically(tween(450)) { 50 })
                            .togetherWith(fadeOut(tween(300)))
                    }
                    else -> {
                        fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                    }
                }
            },
            label = "AuthAndAppAnimation"
        ) { step ->
            when (step) {
                AuthStep.LOGIN -> {
                    LoginScreen(viewModel = viewModel)
                }
                AuthStep.SIGN_UP -> {
                    BackHandler { viewModel.setAuthStep(AuthStep.LOGIN) }
                    SignUpScreen(viewModel = viewModel)
                }
                AuthStep.GOOGLE_AUTH_SHEET -> {
                    BackHandler { viewModel.setAuthStep(AuthStep.LOGIN) }
                    SocialAuthScreen(provider = "Google", viewModel = viewModel)
                }
                AuthStep.APPLE_AUTH_SHEET -> {
                    BackHandler { viewModel.setAuthStep(AuthStep.LOGIN) }
                    SocialAuthScreen(provider = "Apple", viewModel = viewModel)
                }
                AuthStep.INSTAGRAM_AUTH_SHEET -> {
                    BackHandler { viewModel.setAuthStep(AuthStep.LOGIN) }
                    SocialAuthScreen(provider = "Instagram", viewModel = viewModel)
                }
                AuthStep.X_AUTH_SHEET -> {
                    BackHandler { viewModel.setAuthStep(AuthStep.LOGIN) }
                    SocialAuthScreen(provider = "Twitter-X", viewModel = viewModel)
                }
                AuthStep.SOCIAL_CALLBACK -> {
                    SocialAuthScreen(
                        provider = "Connect",
                        viewModel = viewModel,
                        inProgressText = "Completing secure sign-in..."
                    )
                }
                AuthStep.ACCOUNT_CREATED_SUCCESS -> {
                    AccountCreatedScreen(viewModel = viewModel)
                }
                AuthStep.AUTHENTICATED -> {
                    MainAppScaffold(
                        viewModel = viewModel,
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        onOpenChat = { dealId -> activeChatDeal = dealId }
                    )
                }
            }
        }

        // Create Collab / Post Bottom Sheet
        if (showCreateSheet) {
            CreateCollabSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeCreateSheet() }
            )
        }

        // In-App Notification Floating Glass Bubble
        AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn(tween(250)) +
                    scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) +
                    slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { -it / 2 },
            exit = fadeOut(tween(200)) +
                   scaleOut(tween(200)) +
                   slideOutVertically(tween(200)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            notification?.let { notif ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0x70FFFFFF),
                            ambientColor = Color(0x60000000)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x55353535),
                                    Color(0x801A1A1A),
                                    Color(0xE6080808)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x99FFFFFF),
                                    Color(0x35FFFFFF),
                                    Color(0x18FFFFFF)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("in_app_notification_bubble")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glass icon bubble
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFE0E0E0)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.White, Color(0x80FFFFFF))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Notification",
                                tint = PureBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = notif.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Glass dismiss button
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0x25FFFFFF), Color(0x10FFFFFF))
                                    )
                                )
                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                .clickable { viewModel.dismissNotification() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = PureWhite.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chat overlay (full screen, sits on top of the whole app)
        if (authStep == AuthStep.AUTHENTICATED) {
            activeChatDeal?.let { dealId ->
                ChatScreen(
                    dealId = dealId,
                    chatViewModel = chatViewModel,
                    onNavigateBack = { activeChatDeal = null },
                    currentUserId = AppGraph.repository.getCurrentUserId()
                )
            }
        }

        // App lock overlay (blocks everything until biometric unlock)
        if (appLocked) {
            AppLockOverlay(onUnlock = onRequestUnlock)
        }
    }
}

@Composable
private fun AppLockOverlay(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF101010),
                        Color(0xFF060606)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x40FFFFFF),
                                Color(0x12FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, Color(0x44FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = PureWhite,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Kolab is locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unlock with your fingerprint or device PIN to continue.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhite)
                    .clickable(onClick = onUnlock)
                    .padding(horizontal = 28.dp, vertical = 14.dp)
                    .testTag("app_lock_unlock_button")
            ) {
                Text(
                    text = "Unlock",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }
        }
    }
}

@Composable
private fun MainAppScaffold(
    viewModel: KolabViewModel,
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onOpenChat: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            KolabBottomBar(
                currentTab = currentTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(tween(250)).togetherWith(fadeOut(tween(150)))
                },
                label = "TabContentAnimation"
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> HomeScreen(viewModel = viewModel)
                    NavigationTab.DISCOVER -> DiscoverScreen(viewModel = viewModel)
                    NavigationTab.PLACES -> PlacesScreen(viewModel = viewModel)
                    NavigationTab.CREATE -> HomeScreen(viewModel = viewModel)
                    NavigationTab.PROFILE -> ProfileScreen(viewModel = viewModel, onOpenChat = onOpenChat)
                }
            }
        }
    }
}

