package com.example.ui.frames

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.FeedPost
import com.example.ui.KolabViewModel
import com.example.ui.components.ShimmerBlock
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Full-screen, swipe-up feed for photos and short videos. One post is one
 * Frame. Views are recorded once per page; guests are counted too.
 */
@Composable
fun FramesScreen(
    viewModel: KolabViewModel,
    startPostId: String?,
    onClose: () -> Unit
) {
    val posts by viewModel.feedPosts.collectAsState()
    val media = remember(posts) { posts.filter { it.hasMedia } }
    var muted by remember { mutableStateOf(true) }
    val context = LocalContext.current

    BackHandler(onBack = onClose)

    if (media.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Frames yet",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When people share photos or videos, they'll appear here.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Close",
                    color = PureBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PureWhite)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
        return
    }

    val initialPage = remember(startPostId, media.size) {
        media.indexOfFirst { it.id == startPostId }.takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { media.size }

    LaunchedEffect(pagerState.currentPage, media.size) {
        media.getOrNull(pagerState.currentPage)?.let { viewModel.recordFrameView(it.id) }
    }

    Box(modifier = Modifier.fillMaxSize().background(PureBlack)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val post = media[page]
            val active = pagerState.currentPage == page
            FramePage(
                post = post,
                active = active,
                muted = muted,
                onLike = { viewModel.toggleLike(post.id) },
                onComments = { viewModel.openComments(post) },
                onShare = { shareFrame(context, post) },
                onDoubleTap = { if (!post.isLiked) viewModel.toggleLike(post.id) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close Frames", tint = PureWhite)
            }
            Text(
                text = "Frames",
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { muted = !muted }) {
                Icon(
                    imageVector = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (muted) "Unmute" else "Mute",
                    tint = PureWhite
                )
            }
        }
    }
}

@Composable
private fun FramePage(
    post: FeedPost,
    active: Boolean,
    muted: Boolean,
    onLike: () -> Unit,
    onComments: () -> Unit,
    onShare: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val look = remember(post.filter) { FrameLook.fromKey(post.filter) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(post.id) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
    ) {
        if (post.videoUrl.isNotBlank()) {
            FrameVideoPlayer(
                url = post.videoUrl,
                playWhenReady = active,
                lookKey = post.filter,
                muted = muted,
                modifier = Modifier.fillMaxSize()
            )
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
                }
            )
        }

        // Readability gradients.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.45f),
                        0.25f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 76.dp, bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .border(1.dp, PureWhite.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.take(1).uppercase(),
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = post.author,
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.location.isNotBlank()) {
                        Text(
                            text = post.location,
                            color = PureWhite.copy(alpha = 0.72f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    color = PureWhite,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FramePill(text = "FRAME")
                FramePill(text = look.label.uppercase())
                FramePill(text = formatCount(post.views) + " views")
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FrameAction(
                icon = {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (post.isLiked) "Unlike" else "Like",
                        tint = if (post.isLiked) Color(0xFFFF4D6D) else PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = formatCount(post.likes),
                onClick = onLike
            )
            FrameAction(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Comments",
                        tint = PureWhite,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = formatCount(post.comments),
                onClick = onComments
            )
            FrameAction(
                icon = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = PureWhite,
                        modifier = Modifier.size(25.dp)
                    )
                },
                label = "Share",
                onClick = onShare
            )
            FrameAction(
                icon = {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = formatCount(post.views),
                onClick = {}
            )
        }

        if (post.videoUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PureWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun FrameAction(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = label,
            color = PureWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FramePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, PureWhite.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

/** Horizontal "Frames" strip for the home feed. */
@Composable
fun FramesRail(
    posts: List<FeedPost>,
    onOpen: (FeedPost) -> Unit,
    onSeeAll: () -> Unit
) {
    if (posts.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Frames",
                color = PureWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(posts.take(12), key = { it.id }) { post ->
                val look = remember(post.filter) { FrameLook.fromKey(post.filter) }
                Box(
                    modifier = Modifier
                        .size(width = 112.dp, height = 158.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, PureWhite.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .clickable { onOpen(post) }
                ) {
                    if (post.imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.description,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1B1B1B))
                                .padding(bottom = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video Frame",
                                tint = PureWhite.copy(alpha = 0.75f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 70f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = post.author,
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = look.label,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(7.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FRAME",
                            color = PureWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun shareFrame(context: Context, post: FeedPost) {
    val text = buildString {
        if (post.description.isNotBlank()) {
            append(post.description)
            append("\n\n")
        }
        append("See this Frame on Kolab: https://wiz-cmd.github.io/kolab/")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "A Frame on Kolab")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share this Frame"))
}

private fun formatCount(value: Int): String = when {
    value >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000f)
    value >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", value / 1_000f)
    else -> value.toString()
}
