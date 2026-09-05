package com.example.ui.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CreativeProfile
import com.example.data.FeedPost
import com.example.ui.KolabViewModel
import com.example.ui.components.CollabRequestBottomSheet
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DiscoverScreen(viewModel: KolabViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCreativeCategory.collectAsState()
    val creatives by viewModel.filteredCreatives.collectAsState()
    val targetCreative by viewModel.collabTargetCreative.collectAsState()
    val searchResultsState by viewModel.searchResults.collectAsState()
    val followingIds by viewModel.followingIds.collectAsState()

    var detailCreative by remember { mutableStateOf<CreativeProfile?>(null) }

    val isSearching = searchQuery.trim().length >= 2

    val recentSearches = remember(searchQuery) {
        if (searchQuery.isBlank()) viewModel.recentSearches() else emptyList()
    }

    val categories = listOf(
        "All Creatives",
        "Models",
        "Photographers",
        "Videographers",
        "Location",
        "Rates"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Header
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search creators, models, locations...", color = TextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("discover_search_input"),
                        shape = RoundedCornerShape(24.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh,
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = DividerBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, DividerBorder, CircleShape)
                            .clickable {
                                viewModel.showNotification("Filter preferences: All active in South Africa")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Category Filter Pills (Glassmorphic)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val bgBrush = if (isSelected) {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                PureWhite,
                                Color(0xFFEEEEEE)
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0x2EFFFFFF),
                                Color(0x12FFFFFF)
                            )
                        )
                    }

                    val borderBrush = if (isSelected) {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xCCFFFFFF)
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0x44FFFFFF),
                                Color(0x15FFFFFF)
                            )
                        )
                    }

                    val textColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isSelected) PureBlack else TextPrimary,
                        animationSpec = androidx.compose.animation.core.tween(250),
                        label = "pillTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgBrush)
                            .border(1.dp, borderBrush, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setCreativeCategory(cat) }
                            .padding(horizontal = 15.dp, vertical = 8.dp)
                            .testTag("filter_chip_$cat")
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            // Results: live ranked search when the query is active, cached
            // creatives (+ recents) otherwise.
            if (isSearching) {
                SearchResultsSection(
                    results = searchResultsState,
                    followingIds = followingIds,
                    onFollow = { id, name -> viewModel.toggleFollow(id, name) },
                    onRequestCollab = { creative ->
                        viewModel.setSearchQuery("")
                        viewModel.openCollabRequest(creative)
                    }
                )
            } else {
                SocialSearchRecentsRow(
                    recents = recentSearches,
                    onSelect = { viewModel.setSearchQuery(it) },
                    onClear = { viewModel.clearRecentSearches() }
                )

                androidx.compose.animation.AnimatedContent(
                    targetState = selectedCategory,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)) +
                         androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(250)) { 20 })
                        .togetherWith(androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150)))
                    },
                    label = "DiscoverCategoryContentAnimation",
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("creatives_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(creatives, key = { it.id }) { creative ->
                            CreativeCard(
                                creative = creative,
                                isFollowing = followingIds.contains(creative.id),
                                onFollowToggle = { viewModel.toggleFollow(creative.id, creative.name) },
                                onCardClick = { detailCreative = creative },
                                onRequestCollab = { viewModel.openCollabRequest(creative) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }

        // Collab Request Bottom Sheet
        targetCreative?.let { creative ->
            CollabRequestBottomSheet(
                creative = creative,
                onDismiss = { viewModel.closeCollabRequest() },
                onSubmit = { request -> viewModel.submitCollabRequest(request) }
            )
        }

        // Detail Bottom Sheet
        detailCreative?.let { creative ->
            CreativeDetailBottomSheet(
                creative = creative,
                isFollowing = followingIds.contains(creative.id),
                onFollowToggle = { viewModel.toggleFollow(creative.id, creative.name) },
                onDismiss = { detailCreative = null },
                onRequestCollab = {
                    detailCreative = null
                    viewModel.openCollabRequest(creative)
                }
            )
        }
    }
}

@Composable
private fun CreativeCard(
    creative: CreativeProfile,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onCardClick: () -> Unit,
    onRequestCollab: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .padding(16.dp)
            .testTag("creative_card_${creative.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = creative.imageUrl,
                    contentDescription = creative.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creative.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = creative.role,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = creative.location,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Rating Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = StarGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${creative.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rate and Request CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rate",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = creative.rate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                Button(
                    onClick = onRequestCollab,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "Request Collab",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFollowing) SurfaceContainerHigh else PureWhite)
                        .border(1.dp, DividerBorder, RoundedCornerShape(10.dp))
                        .clickable(onClick = onFollowToggle)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFollowing) PureWhite else PureBlack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreativeDetailBottomSheet(
    creative: CreativeProfile,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onDismiss: () -> Unit,
    onRequestCollab: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = PureBlack.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = creative.imageUrl,
                        contentDescription = creative.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(1.dp, DividerBorder, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = creative.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = creative.role,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = creative.bio,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = creative.followers,
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
                        text = "${creative.collabsCount}",
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
                        text = "${creative.rating} ★",
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestCollab,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text(
                    text = "Request Collaboration (${creative.rate})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isFollowing) SurfaceContainerHigh else CardBackground)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onFollowToggle)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFollowing) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isFollowing) "Following" else "Follow ${creative.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFollowing) TextSecondary else PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SocialSearchRecentsRow(
    recents: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    if (recents.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Recent:",
            fontSize = 11.sp,
            color = TextSecondary
        )
        recents.forEach { r ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerHigh)
                    .clickable { onSelect(r) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = r,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Clear",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClear)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SearchResultsSection(
    results: com.example.ui.SearchResults,
    followingIds: Set<String>,
    onFollow: (String, String) -> Unit,
    onRequestCollab: (CreativeProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (results.isLoading) {
                    "Searching \"${results.searchedQuery}\"..."
                } else {
                    "Results for \"${results.searchedQuery}\""
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (results.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            if (results.users.isNotEmpty()) {
                item {
                    Text(
                        text = "Creators",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                items(results.users, key = { "user_${it.id}" }) { creative ->
                    SearchResultUserRow(
                        creative = creative,
                        isFollowing = followingIds.contains(creative.id),
                        onFollowToggle = { onFollow(creative.id, creative.name) },
                        onRequestCollab = { onRequestCollab(creative) }
                    )
                }
            }

            if (results.posts.isNotEmpty()) {
                item {
                    Text(
                        text = "Posts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                items(results.posts, key = { "post_${it.id}" }) { post ->
                    SearchPostCard(post = post)
                }
            }

            if (results.users.isEmpty() && results.posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results. Try a different name, role or topic.",
                            fontSize = 13.sp,
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
}

@Composable
private fun SearchResultUserRow(
    creative: CreativeProfile,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onRequestCollab: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = creative.imageUrl,
                contentDescription = creative.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(1.dp, DividerBorder, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = creative.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Text(
                    text = creative.role,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isFollowing) SurfaceContainerHigh else PureWhite)
                    .clickable(onClick = onFollowToggle)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFollowing) PureWhite else PureBlack
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Collab",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(PureWhite.copy(alpha = 0.15f))
                    .clickable(onClick = onRequestCollab)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun SearchPostCard(post: FeedPost) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.author,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DividerBorder, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
                Text(
                    text = post.description,
                    fontSize = 12.5.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${post.likes} likes  •  $post.timestamp",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
