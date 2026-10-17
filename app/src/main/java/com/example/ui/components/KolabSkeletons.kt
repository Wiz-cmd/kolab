package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder

/**
 * Shimmer placeholders that mirror the real Kolab cards. They reuse the exact
 * glass chrome (same corner radii, borders and paddings) so, when content
 * arrives, the card appears to "fill in" rather than shift.
 *
 * Each skeleton takes a single shared shimmer brush so every bar sweeps in
 * lock-step, and so a screen full of placeholders only drives one animation.
 */

@Composable
fun KolabFeedPostSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberKolabShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBlock(Modifier.size(38.dp), CircleShape, brush)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    ShimmerBlock(Modifier.width(132.dp).height(13.dp), RoundedCornerShape(6.dp), brush)
                    Spacer(modifier = Modifier.height(7.dp))
                    ShimmerBlock(Modifier.width(84.dp).height(10.dp), RoundedCornerShape(6.dp), brush)
                }
            }

            ShimmerBlock(Modifier.fillMaxWidth().height(240.dp), RoundedCornerShape(0.dp), brush)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ShimmerBlock(Modifier.fillMaxWidth().height(12.dp), RoundedCornerShape(6.dp), brush)
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBlock(Modifier.fillMaxWidth(0.7f).height(12.dp), RoundedCornerShape(6.dp), brush)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShimmerBlock(Modifier.size(20.dp), CircleShape, brush)
                        Spacer(modifier = Modifier.width(8.dp))
                        ShimmerBlock(Modifier.width(40.dp).height(12.dp), RoundedCornerShape(6.dp), brush)
                    }
                    ShimmerBlock(Modifier.width(104.dp).height(38.dp), RoundedCornerShape(10.dp), brush)
                }
            }
        }
    }
}

@Composable
fun KolabCreativeCardSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberKolabShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBlock(Modifier.size(72.dp), RoundedCornerShape(16.dp), brush)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBlock(Modifier.width(150.dp).height(15.dp), RoundedCornerShape(6.dp), brush)
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBlock(Modifier.width(100.dp).height(12.dp), RoundedCornerShape(6.dp), brush)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShimmerBlock(Modifier.width(112.dp).height(11.dp), RoundedCornerShape(6.dp), brush)
                        Spacer(modifier = Modifier.width(10.dp))
                        ShimmerBlock(Modifier.width(52.dp).height(18.dp), RoundedCornerShape(9.dp), brush)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            ShimmerBlock(Modifier.fillMaxWidth().height(12.dp), RoundedCornerShape(6.dp), brush)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBlock(Modifier.fillMaxWidth(0.85f).height(12.dp), RoundedCornerShape(6.dp), brush)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerBlock(Modifier.weight(1f).height(40.dp), RoundedCornerShape(10.dp), brush)
                ShimmerBlock(Modifier.weight(1f).height(40.dp), RoundedCornerShape(10.dp), brush)
            }
        }
    }
}

@Composable
fun KolabHotspotCardSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberKolabShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ShimmerBlock(Modifier.fillMaxWidth().height(180.dp), RoundedCornerShape(0.dp), brush)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        ShimmerBlock(Modifier.width(150.dp).height(15.dp), RoundedCornerShape(6.dp), brush)
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerBlock(Modifier.width(110.dp).height(11.dp), RoundedCornerShape(6.dp), brush)
                    }
                    ShimmerBlock(Modifier.width(96.dp).height(36.dp), RoundedCornerShape(10.dp), brush)
                }
                Spacer(modifier = Modifier.height(14.dp))
                ShimmerBlock(Modifier.fillMaxWidth().height(11.dp), RoundedCornerShape(6.dp), brush)
                Spacer(modifier = Modifier.height(7.dp))
                ShimmerBlock(Modifier.fillMaxWidth(0.75f).height(11.dp), RoundedCornerShape(6.dp), brush)
            }
        }
    }
}

@Composable
fun KolabSearchRowSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberKolabShimmerBrush()
    Box(
        modifier = modifier
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
            ShimmerBlock(Modifier.size(52.dp), CircleShape, brush)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBlock(Modifier.width(120.dp).height(13.dp), RoundedCornerShape(6.dp), brush)
                Spacer(modifier = Modifier.height(7.dp))
                ShimmerBlock(Modifier.width(80.dp).height(10.dp), RoundedCornerShape(6.dp), brush)
            }
            ShimmerBlock(Modifier.width(72.dp).height(30.dp), RoundedCornerShape(9.dp), brush)
            Spacer(modifier = Modifier.width(8.dp))
            ShimmerBlock(Modifier.width(60.dp).height(30.dp), RoundedCornerShape(9.dp), brush)
        }
    }
}

/** A 3-column grid of square tiles, matching the profile "Portfolio Grid". */
@Composable
fun KolabPortfolioGridSkeleton(rows: Int = 2, modifier: Modifier = Modifier) {
    val brush = rememberKolabShimmerBrush()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    ShimmerBlock(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}
