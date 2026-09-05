package com.example.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FeedMode
import com.example.data.FeedPost
import com.example.ui.KolabViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.KolabTopBar
import com.example.ui.components.LocationPickerDialog
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(viewModel: KolabViewModel) {
    val feedPosts by viewModel.feedPosts.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val feedMode by viewModel.feedMode.collectAsState()
    var showLocationPicker by remember { mutableStateOf(false) }

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
                        onNewPost = { viewModel.openCreateSheet() },
                        onFindCollab = { viewModel.selectTab(NavigationTab.DISCOVER) },
                        onHotspots = { viewModel.selectTab(NavigationTab.PLACES) },
                        onDeals = { viewModel.selectTab(NavigationTab.PROFILE) }
                    )
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
                                text = "Active Calls",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FeedModeToggle(
                            mode = feedMode,
                            onSelect = { viewModel.setFeedMode(it) }
                        )
                    }
                }

                // Feed Post Cards
                items(feedPosts, key = { it.id }) { post ->
                    FeedPostCard(
                        post = post,
                        onLike = { viewModel.toggleLike(post.id) },
                        onCollab = {
                            viewModel.showNotification("Connected with ${post.author}. Collab pitch opened.")
                            viewModel.openCreateSheet()
                        }
                    )
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
                containerColor = PureWhite,
                contentColor = PureBlack,
                shape = CircleShape,
                modifier = Modifier.testTag("home_create_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Collab",
                    tint = PureBlack,
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
                    .background(if (selected) PureWhite else Color.Transparent)
                    .clickable { onSelect(m) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (m == FeedMode.FOR_YOU) "For You" else "Following",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) PureBlack else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BentoActionsGrid(
    onNewPost: () -> Unit,
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
                title = "New Post",
                subtitle = "Share moodboard",
                icon = Icons.Default.AddBox,
                modifier = Modifier.weight(1f),
                onClick = onNewPost,
                testTag = "bento_new_post"
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
            .clickable(onClick = onClick)
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
    onLike: () -> Unit,
    onCollab: () -> Unit
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
            }

            // Media Image with 16:10 ratio & gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
                    // Likes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onLike)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.likes}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    // Collab Action Button
                    Button(
                        onClick = onCollab,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = "Collab",
                                tint = PureBlack,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Collab",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                    }
                }
            }
        }
    }
}
