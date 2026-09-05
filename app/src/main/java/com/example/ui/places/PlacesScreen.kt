package com.example.ui.places

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
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.Hotspot
import com.example.data.LeaderboardItem
import com.example.data.TrendDirection
import com.example.ui.KolabViewModel
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
import com.example.ui.theme.TrendGreen

@Composable
fun PlacesScreen(viewModel: KolabViewModel) {
    val hotspots by viewModel.hotspots.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val selectedCategory by viewModel.selectedHotspotCategory.collectAsState()
    val isMapView by viewModel.isMapViewEnabled.collectAsState()

    val categories = listOf(
        "ALL",
        "BEST FOR PHOTOSHOOTS",
        "GOLDEN HOUR",
        "STUDIO VIBES",
        "STREET EDITORIAL"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Places Top Bar
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kolab",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Map Toggle Pill (Glassmorphic)
                        val mapBgBrush = if (isMapView) {
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(PureWhite, Color(0xFFE8E8E8))
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0x35FFFFFF), Color(0x15FFFFFF))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(mapBgBrush)
                                .border(
                                    1.dp,
                                    if (isMapView) Color.White else Color(0x35FFFFFF),
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { viewModel.toggleMapView() }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("map_view_toggle")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map",
                                    tint = if (isMapView) PureBlack else PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isMapView) "LIST" else "MAP",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMapView) PureBlack else PureWhite
                                )
                            }
                        }

                        // Search Icon (Glassmorphic)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0x30FFFFFF), Color(0x10FFFFFF))
                                    )
                                )
                                .border(1.dp, Color(0x30FFFFFF), CircleShape)
                                .clickable {
                                    viewModel.showNotification("Searching creative shoot hotspots...")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Category Chips (Glassmorphic)
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
                            colors = listOf(PureWhite, Color(0xFFEEEEEE))
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0x28FFFFFF), Color(0x10FFFFFF))
                        )
                    }

                    val borderBrush = if (isSelected) {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xCCFFFFFF))
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0x40FFFFFF), Color(0x15FFFFFF))
                        )
                    }

                    val textColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isSelected) PureBlack else TextPrimary,
                        animationSpec = androidx.compose.animation.core.tween(250),
                        label = "placesCatColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgBrush)
                            .border(1.dp, borderBrush, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setHotspotCategory(cat) }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            if (isMapView) {
                // Interactive Simulated Map View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map view",
                            tint = PureWhite,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "South Africa Creator Heatmap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Johannesburg (38 spots) • Cape Town (52 spots)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.toggleMapView() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Switch to List View", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Hotspots and Leaderboard List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("places_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(hotspots, key = { it.id }) { spot ->
                        HotspotCard(
                            hotspot = spot,
                            onCheckIn = { viewModel.toggleCheckIn(spot.id) }
                        )
                    }

                    // Provincial Leaderboard Section
                    item {
                        LeaderboardSection(leaderboard = leaderboard)
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HotspotCard(
    hotspot: Hotspot,
    onCheckIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
            .testTag("hotspot_card_${hotspot.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image with rating pill and location badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = hotspot.imageUrl,
                    contentDescription = hotspot.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating overlay badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureBlack.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = StarGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${hotspot.rating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name & Check-in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = hotspot.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = hotspot.location,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onCheckIn,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hotspot.isCheckedIn) SurfaceContainerHigh else PureWhite,
                            contentColor = if (hotspot.isCheckedIn) PureWhite else PureBlack
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hotspot.isCheckedIn) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Checked In",
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (hotspot.isCheckedIn) "Checked In (${hotspot.checkInsCount})" else "Check-in",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Creator Tip Quote Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "CREATOR TIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hotspot.creatorTip,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardSection(leaderboard: List<LeaderboardItem>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("provincial_leaderboard_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Provincial Leaderboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Text(
                text = "Most active creator hubs this month.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            leaderboard.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.rank}.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.rank == 1) StarGold else TextSecondary,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = item.province,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.checkIns,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        when (item.trend) {
                            TrendDirection.UP -> Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trending Up",
                                tint = TrendGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            TrendDirection.STEADY -> Icon(
                                imageVector = Icons.Default.TrendingFlat,
                                contentDescription = "Steady",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            TrendDirection.DOWN -> Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "Trending Down",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (index < leaderboard.size - 1) {
                    HorizontalDivider(color = DividerBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}
