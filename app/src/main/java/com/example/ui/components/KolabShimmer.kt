package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBackground
import com.example.ui.theme.SurfaceContainerHigh

/**
 * A soft blade of light that sweeps across frosted-glass placeholders while
 * content loads. Built on the Kolab Noir palette, so skeletons read as light
 * moving through frost — never a flat grey box. Hand-rolled with the Compose
 * animation APIs: no third-party shimmer dependency.
 */
@Composable
fun rememberKolabShimmerBrush(
    base: Color = CardBackground,
    highlight: Color = SurfaceContainerHigh,
    durationMillis: Int = 1400
): Brush {
    val transition = rememberInfiniteTransition(label = "kolabShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "kolabShimmerSweep"
    )
    // Slide the highlight band from well off the left edge to off the right.
    val travel = 1200f
    val start = -travel + progress * (travel * 2f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(start, 0f),
        end = Offset(start + travel, travel)
    )
}

/** A single frosted placeholder block filled with the Kolab shimmer. */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = rememberKolabShimmerBrush()
) {
    Box(modifier = modifier.clip(shape).background(brush))
}
