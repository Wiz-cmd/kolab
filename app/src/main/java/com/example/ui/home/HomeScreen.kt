package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.data.AppGraph
import com.example.data.CreativeProfile
import com.example.data.FeedMode
import com.example.data.FeedPost
import com.example.ui.KolabViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.KolabFeedPostSkeleton
import com.example.ui.components.KolabLikeButton
import com.example.ui.components.KolabPrimaryButton
import com.example.ui.components.KolabTopBar
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.ShimmerBlock
import com.example.ui.components.kolabClickable
import com.example.ui.frames.FramesRail
import com.example.ui.frames.shareFrame
import com.example.ui.theme.Accent
import com.example.ui.theme.AccentBorder
import com.example.ui.theme.AccentText
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.OnAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(viewModel: KolabViewModel) {
    val feedPosts by viewModel.feedPosts.collectAsState()
    val feedLoading by viewModel.feedLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val feedMode by viewModel.feedMode.collectAsState()
    val editingPost by viewModel.editingPost.collectAsState()
    val challenge by viewModel.currentChallenge.collectAsState()
    val challengeLeaders by viewModel.challengeLeaderboard.collectAsState()
    val risingCreators by viewModel.risingCreators.collectAsState()
    var showLocationPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val myUserId = AppGraph.repository.getCurrentUserId()
    val frames = remember(feedPosts) { feedPosts.filter { it.hasMedia } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            KolabTopBar(
                location = currentLocation,
                onLocationClick = { showLocationPicker = true },
                onSearchClick = { viewModel.selectTab(NavigationTab.DISCOVER) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_feed_lazy_column"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Bento Quick Actions (2x2 Grid)
                item {
                    BentoActionsGrid(
                        onNewFrame = { viewModel.openFrameCamera() },
                        onFindCollab = { viewModel.selectTab(NavigationTab.DISCOVER) },
                        onHotspots = { viewModel.selectTab(NavigationTab.PLACES) },
                        onDeals = { viewModel.selectTab(NavigationTab.PROFILE) }
                    )
                }

                // Weekly creative challenge — the daily reason to post
                challenge?.let { ch ->
                    item {
                        ChallengeCard(
                            title = ch.title,
                            brief = ch.brief,
                            city = ch.city,
                            leaders = challengeLeaders.take(3).map { it.description }
                        )
                    }
                }

                // Rising creators near the user
                if (risingCreators.isNotEmpty()) {
                    item {
                        RisingCreatorsCard(
                            creators = risingCreators.take(6),
                            city = currentLocation.substringBefore(',')
                        )
                    }
                }

                // Frames strip: photos and short videos from the community
                if (frames.isNotEmpty()) {
                    item {
                        FramesRail(
                            posts = frames,
                            onOpen = { viewModel.openFramesViewer(it.id) },
                            onSeeAll = { viewModel.openFramesViewer(null) }
                        )
                    }
                }

                // Feed Section Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (feedMode == FeedMode.FOR_YOU) "For You" else "Following",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = if (frames.isEmpty()) {
                                    "Near ${currentLocation.substringBefore(',')}"
                                } else {
                                    "${frames.size} Frames · ${currentLocation.substringBefore(',')}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FeedModeToggle(
                            mode = feedMode,
                            onSelect = { viewModel.setFeedMode(it) }
                        )
                    }
                }

                // Feed Post Cards — shimmer placeholders while the first page loads
                if (feedLoading && feedPosts.isEmpty()) {
                    items(3) { KolabFeedPostSkeleton() }
                } else {
                    items(feedPosts, key = { it.id }) { post ->
                        FeedPostCard(
                            post = post,
                            isMine = myUserId.isNotBlank() && post.authorId == myUserId,
                            onLike = { viewModel.toggleLike(post.id) },
                            onCollab = { viewModel.selectTab(NavigationTab.DISCOVER) },
                            onComment = { viewModel.openComments(post) },
                            onShare = { shareFrame(context, post) },
                            onOpenFrame = { viewModel.openFramesViewer(post.id) },
                            onEdit = { viewModel.openEditPost(post) },
                            onDelete = { viewModel.deleteOwnPost(post) }
                        )
                    }
                }

                if (!feedLoading && feedPosts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nothing in your feed yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Follow creatives or check back soon for new work.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.openCreateSheet() },
                containerColor = Accent,
                contentColor = OnAccent,
                shape = CircleShape,
                modifier = Modifier.testTag("home_create_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Collab",
                    tint = OnAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showLocationPicker) {
            LocationPickerDialog(
                currentLocation = currentLocation,
                onLocationSelected = { viewModel.switchLocation(it) },
                onDismiss = { showLocationPicker = false }
            )
        }

        editingPost?.let { post ->
            EditFrameDialog(
                post = post,
                onDismiss = { viewModel.closeEditPost() },
                onSave = { caption, place -> viewModel.saveEditedPost(caption, place) }
            )
        }
    }
}

@Composable
private fun FeedModeToggle(
    mode: FeedMode,
    onSelect: (FeedMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FeedMode.entries.forEach { m ->
            val selected = mode == m
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Accent else Color.Transparent)
                    .kolabClickable { onSelect(m) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (m == FeedMode.FOR_YOU) "For You" else "Following",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) OnAccent else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BentoActionsGrid(
    onNewFrame: () -> Unit,
    onFindCollab: () -> Unit,
    onHotspots: () -> Unit,
    onDeals: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoCard(
                title = "New Frame",
                subtitle = "Photo or video",
                icon = Icons.Default.CameraAlt,
                modifier = Modifier.weight(1f),
                onClick = onNewFrame,
                testTag = "bento_new_frame"
            )
            BentoCard(
                title = "Find Collab",
                subtitle = "Browse creatives",
                icon = Icons.Default.Handshake,
                modifier = Modifier.weight(1f),
                onClick = onFindCollab,
                testTag = "bento_find_collab"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoCard(
                title = "Hotspots",
                subtitle = "Shoot locations",
                icon = Icons.Default.Explore,
                modifier = Modifier.weight(1f),
                onClick = onHotspots,
                testTag = "bento_hotspots"
            )
            BentoCard(
                title = "My Deals",
                subtitle = "Active contracts",
                icon = Icons.Default.LocalOffer,
                modifier = Modifier.weight(1f),
                onClick = onDeals,
                testTag = "bento_my_deals"
            )
        }
    }
}

@Composable
private fun BentoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
            .kolabClickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FeedPostCard(
    post: FeedPost,
    isMine: Boolean,
    onLike: () -> Unit,
    onCollab: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onOpenFrame: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
            .testTag("feed_post_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Author row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, DividerBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.take(1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.author,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = TextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.location,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (post.isStudioOrGear) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STUDIO RENTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }

                if (isMine) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Frame",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .kolabClickable(onClick = onEdit)
                                .testTag("edit_post_${post.id}")
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Frame",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .kolabClickable(onClick = onDelete)
                                .testTag("delete_post_${post.id}")
                        )
                    }
                }
            }

            // Media — photo or short video Frame
            if (post.hasMedia) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .kolabClickable(onClick = onOpenFrame)
                ) {
                    if (post.videoUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF121212)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play video Frame",
                                tint = PureWhite.copy(alpha = 0.85f),
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    } else {
                        SubcomposeAsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.description,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                ShimmerBlock(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(0.dp)
                                )
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Media unavailable",
                                        tint = PureWhite.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        )
                    }
                    // Bottom subtle gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, AccentBorder, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "FRAME",
                                color = AccentText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (post.videoUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "VIDEO",
                                    color = PureWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = PureWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.views}",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Description and actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = post.description,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Likes — animated, optimistic
                        KolabLikeButton(
                            isLiked = post.isLiked,
                            likes = post.likes,
                            onToggle = onLike
                        )
                        FeedMetric(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            label = if (post.comments > 0) "${post.comments}" else "Comment",
                            onClick = onComment
                        )
                        FeedMetric(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = onShare
                        )
                        FeedMetric(
                            icon = Icons.Default.Visibility,
                            label = if (post.views > 0) "${post.views}" else "Views",
                            onClick = onOpenFrame
                        )
                    }

                    // Collab Action Button
                    KolabPrimaryButton(
                        text = "Collab",
                        icon = Icons.Default.Handshake,
                        onClick = onCollab,
                        height = 34.dp,
                        shape = RoundedCornerShape(10.dp),
                        fontSize = 11
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    title: String,
    brief: String,
    city: String,
    leaders: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(AccentSoft, CardBackground)))
            .border(1.5.dp, AccentBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("WEEKLY CHALLENGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentText, modifier = Modifier.weight(1f))
            if (city.isNotBlank() && city != "Anywhere") {
                Text(city.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentText)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PureWhite)
        Text(brief, fontSize = 12.sp, color = TextSecondary)
        if (leaders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            leaders.forEachIndexed { index, caption ->
                Text(
                    text = "${index + 1}. ${caption.take(48)}",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun RisingCreatorsCard(creators: List<CreativeProfile>, city: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.5.dp, AccentBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Accent))
            Spacer(modifier = Modifier.width(7.dp))
            Text("Rising in $city", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PureWhite)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            creators.take(4).forEach { creator ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, DividerBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(creator.name.take(1), fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(creator.name.take(10), fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                    if (creator.badges.isNotEmpty()) {
                        Text(
                            creator.badges.first(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (creator.availableForCollab) AccentText else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditFrameDialog(
    post: FeedPost,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var caption by remember(post.id) { mutableStateOf(post.description) }
    var place by remember(post.id) { mutableStateOf(post.location) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Edit Frame",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = { if (it.length <= 4000) caption = it },
                label = { Text("Caption") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = place,
                onValueChange = { if (it.length <= 200) place = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.kolabClickable(onClick = onDismiss)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Save",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    modifier = Modifier.kolabClickable {
                        if (caption.isNotBlank()) onSave(caption, place)
                    }
                )
            }
        }
    }
}

@Composable
private fun FeedMetric(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .kolabClickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
