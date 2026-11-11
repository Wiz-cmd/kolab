package com.example.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.ActiveDeal
import com.example.ui.AuthStep
import com.example.ui.KolabViewModel
import com.example.ui.NavigationTab
import com.example.ui.ProfileSubTab
import com.example.ui.components.KolabPortfolioGridSkeleton
import com.example.ui.components.ShimmerBlock
import com.example.ui.components.WithdrawDialog
import com.example.ui.components.kolabClickable
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrendGreen

private const val PRIVACY_POLICY_URL = "https://wiz-cmd.github.io/kolab/legal/privacy.html"
private const val TERMS_OF_SERVICE_URL = "https://wiz-cmd.github.io/kolab/legal/terms.html"

@Composable
fun ProfileScreen(
    viewModel: KolabViewModel,
    onOpenChat: (String) -> Unit = {}
) {
    val profileSubTab by viewModel.profileSubTab.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val activeDeals by viewModel.activeDeals.collectAsState()
    val userPortfolio by viewModel.userPortfolio.collectAsState()
    val portfolioLoading by viewModel.portfolioLoading.collectAsState()
    val showWithdrawDialog by viewModel.showWithdrawDialog.collectAsState()
    val userName by viewModel.currentUserName.collectAsState()
    val isGuest by viewModel.isGuestMode.collectAsState()
    var showSecurity by remember { mutableStateOf(false) }

    if (isGuest) {
        GuestProfileScreen(viewModel)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (showSecurity) {
            SecurityScreen(
                viewModel = viewModel,
                onBack = { showSecurity = false }
            )
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Sub-Tab Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kolab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    // Sub-tab Segmented Control (Glassmorphic)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0x35FFFFFF), Color(0x12FFFFFF))
                                )
                            )
                            .border(
                                1.dp,
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0x50FFFFFF), Color(0x15FFFFFF))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Row {
                            val isStudio = profileSubTab == ProfileSubTab.MY_STUDIO
                            val studioBg = if (isStudio) {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(PureWhite, Color(0xFFE8E8E8))
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }

                            val studioTextColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isStudio) PureBlack else TextSecondary,
                                animationSpec = androidx.compose.animation.core.tween(250),
                                label = "studioTextColor"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(studioBg)
                                    .clickable { viewModel.setProfileSubTab(ProfileSubTab.MY_STUDIO) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("subtab_my_studio")
                            ) {
                                Text(
                                    text = "My Studio",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = studioTextColor
                                )
                            }

                            val isPublic = profileSubTab == ProfileSubTab.PUBLIC_PROFILE
                            val publicBg = if (isPublic) {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(PureWhite, Color(0xFFE8E8E8))
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }

                            val publicTextColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isPublic) PureBlack else TextSecondary,
                                animationSpec = androidx.compose.animation.core.tween(250),
                                label = "publicTextColor"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(publicBg)
                                    .clickable { viewModel.setProfileSubTab(ProfileSubTab.PUBLIC_PROFILE) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("subtab_public_profile")
                            ) {
                                Text(
                                    text = "Public View",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = publicTextColor
                                )
                            }
                        }
                    }

                        IconButton(
                            onClick = { showSecurity = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, DividerBorder, CircleShape)
                                .testTag("open_security_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedContent(
                targetState = profileSubTab,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) +
                     androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(300)) { if (targetState == ProfileSubTab.PUBLIC_PROFILE) it / 3 else -it / 3 })
                    .togetherWith(
                        androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                    )
                },
                label = "ProfileSubTabAnimation"
            ) { subTab ->
                if (subTab == ProfileSubTab.MY_STUDIO) {
                    MyStudioView(
                        userName = userName,
                        balance = availableBalance,
                        activeDeals = activeDeals,
                        portfolio = userPortfolio,
                        portfolioLoading = portfolioLoading,
                        onWithdraw = { viewModel.openWithdrawDialog() },
                        onAddPortfolio = { viewModel.openCreateSheet() },
                        onSignOut = { viewModel.signOut() },
                        onDeleteAccount = { viewModel.deleteAccount() },
                        onNotify = { viewModel.showNotification(it) },
                        onOpenChat = onOpenChat
                    )
                } else {
                    PublicProfileView(
                        userName = userName,
                        onBookNow = { viewModel.openCreateSheet() },
                        onMessage = { viewModel.showNotification("Chat session started with $userName") },
                        portfolio = userPortfolio,
                        portfolioLoading = portfolioLoading
                    )
                }
            }
        }

        if (showWithdrawDialog) {
            WithdrawDialog(
                balance = availableBalance,
                viewModel = viewModel,
                onDismiss = { viewModel.closeWithdrawDialog() }
            )
        }
    }

@Composable
private fun MyStudioView(
    userName: String,
    balance: Double,
    activeDeals: List<ActiveDeal>,
    portfolio: List<String>,
    portfolioLoading: Boolean,
    onWithdraw: () -> Unit,
    onAddPortfolio: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNotify: (String) -> Unit,
    onOpenChat: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_studio_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Creator Header Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&q=80",
                    contentDescription = userName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, DividerBorder, CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "Creative Studio & Payouts",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Available Balance Card (Image 9.png)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .testTag("studio_balance_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Balance",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Next payout: Nov 15",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "R %,.2f".format(balance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onWithdraw,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("withdraw_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        )
                    ) {
                        Text(
                            text = "Withdraw Funds",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureBlack
                        )
                    }
                }
            }
        }

        // Analytics Row (Image 9.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Profile Views",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1.2k",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Engagement",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+15%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrendGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trending",
                                tint = TrendGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Deals Section (Image 9.png)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Active Deals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "${activeDeals.size} active",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeDeals.forEach { deal ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBackground)
                                .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                                .clickable { onOpenChat(deal.id) }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (deal.iconType == "headphones") Icons.Default.Headphones else Icons.Default.CameraAlt,
                                            contentDescription = deal.type,
                                            tint = PureWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = deal.campaign,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = deal.type,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceContainerHigh)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = deal.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (deal.status == "In Progress") StarGold else PureWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = deal.amount,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Open chat",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Tools
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Quick Tools",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                            .clickable { onNotify("Rate card updated (ZAR 2,500/hr)") }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = "Rates",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "My Rates",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                            .clickable { onNotify("Standard NDAs & Licensing contracts loaded") }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Contracts",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Contract Templates",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }

        // Portfolio Grid (Image 9.png)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Portfolio Grid",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "+ Add Work",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier.clickable(onClick = onAddPortfolio)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3-column Grid
                if (portfolioLoading && portfolio.isEmpty()) {
                    KolabPortfolioGridSkeleton(rows = 3)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        portfolio.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { imgUrl ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = imgUrl,
                                            contentDescription = "Portfolio item",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            loading = {
                                                ShimmerBlock(
                                                    modifier = Modifier.fillMaxSize(),
                                                    shape = RoundedCornerShape(0.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SurfaceContainerHigh)
                                                .kolabClickable(onClick = onAddPortfolio),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add image",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legal links
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
            ) {
                LegalRow(
                    icon = Icons.Default.Lock,
                    label = "Privacy Policy",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                    }
                )
                HorizontalDivider(color = DividerBorder)
                LegalRow(
                    icon = Icons.Default.Description,
                    label = "Terms of Service",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL)))
                    }
                )
            }
        }

        // Sign Out Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onSignOut)
                    .padding(16.dp)
                    .testTag("sign_out_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }

        // Delete account (destructive, bottom of the screen)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                    .clickable { showDeleteDialog = true }
                    .padding(16.dp)
                    .testTag("delete_account_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Account",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DangerRed
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = {
                Text(
                    "This permanently deletes your profile, portfolio and personal data, and signs you out. " +
                        "Deals and payments you already took part in are kept in anonymised form for accounting " +
                        "and legal reasons. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

private val DangerRed = Color(0xFFE5484D)

@Composable
private fun GuestProfileScreen(viewModel: KolabViewModel) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .testTag("guest_profile"),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Guest",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You're browsing Kolab without an account. Sign in to book creatives, message people and manage your work.",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.setAuthStep(AuthStep.LOGIN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("guest_sign_in_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite)
            ) {
                Text("Sign in", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.setAuthStep(AuthStep.SIGN_UP) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("guest_create_account_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh)
            ) {
                Text("Create an account", color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "With an account you can",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuestBenefit("Send collaboration requests and book creatives")
                GuestBenefit("Chat securely and track deals and payments")
                GuestBenefit("Publish work, follow creatives and check in at hotspots")
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
            ) {
                LegalRow(
                    icon = Icons.Default.Lock,
                    label = "Privacy Policy",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                    }
                )
                HorizontalDivider(color = DividerBorder)
                LegalRow(
                    icon = Icons.Default.Description,
                    label = "Terms of Service",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL)))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.selectTab(NavigationTab.HOME) }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keep browsing",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun GuestBenefit(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = TrendGreen,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun LegalRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "\u203A",
            fontSize = 18.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun PublicProfileView(
    userName: String,
    onBookNow: () -> Unit,
    onMessage: () -> Unit,
    portfolio: List<String>,
    portfolioLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("public_profile_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card (Image 3.png)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&q=80",
                        contentDescription = userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(2.dp, PureWhite, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$userName 🇿🇦",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Fashion Model & Creative Director",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "14.2K",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "Followers",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "86",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "Collabs",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "4.9 ★",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                            Text(
                                text = "Rating",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // CTA Buttons (Image 3.png)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBookNow,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("book_now_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            )
                        ) {
                            Text(
                                text = "Book Now",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }

                        Button(
                            onClick = onMessage,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("message_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerHigh,
                                contentColor = PureWhite
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = "Message",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Message",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // Measurements Card (Image 3.png)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = "Measurements",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Measurements",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MeasurementItem(label = "Height", value = "178 cm / 5'10\"")
                        MeasurementItem(label = "Bust", value = "84 cm / 33\"")
                        MeasurementItem(label = "Waist", value = "61 cm / 24\"")
                        MeasurementItem(label = "Hips", value = "89 cm / 35\"")
                    }
                }
            }
        }

        // Availability & Rates Cards (Image 3.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Availability",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TrendGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Next: Oct 15",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "My Rates",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R 2,500",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hourly base rate",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Reviews Card (Image 3.png)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Verified Reviews (42)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = StarGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "4.9 / 5.0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View all",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Recent Work 4-Grid (Image 3.png)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Recent Work",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (portfolioLoading && portfolio.isEmpty()) {
                    KolabPortfolioGridSkeleton(rows = 2)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        portfolio.take(2).forEach { img ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                SubcomposeAsyncImage(
                                    model = img,
                                    contentDescription = "Recent work",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        ShimmerBlock(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(0.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        portfolio.drop(2).take(2).forEach { img ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                SubcomposeAsyncImage(
                                    model = img,
                                    contentDescription = "Recent work",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        ShimmerBlock(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(0.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun MeasurementItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PureWhite
        )
    }
}
