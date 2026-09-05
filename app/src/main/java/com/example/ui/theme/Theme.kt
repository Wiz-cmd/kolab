package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KolabDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = CardBackground,
    onPrimaryContainer = PureWhite,
    secondary = TextPrimary,
    onSecondary = PureBlack,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = PureWhite,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    outline = DividerBorder,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    onError = PureWhite
)

/**
 * Frosted-glass backdrop for the whole app: a deep black that brightens toward
 * the top, with a cold highlight drifting from the top-left corner — the
 * "light through frosted glass" base every translucent panel sits on.
 */
@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(GlassBaseTop, GlassBaseMid, GlassBaseBottom),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GlassSheen, Color.Transparent),
                        center = Offset(160f, 0f),
                        radius = 1100f
                    )
                )
        )
    }
}

/**
 * Frosted-glass surface: translucent white fill, hairline light border and a
 * soft drop shadow. Signature glassmorphism treatment for cards and chrome.
 */
fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    fill: Color = CardBackground,
    borderColor: Color = DividerBorder
): Modifier = this
    .shadow(elevation = 10.dp, shape = shape, ambientColor = PureBlack, spotColor = PureBlack)
    .clip(shape)
    .background(fill)
    .border(width = 1.dp, color = borderColor, shape = shape)

@Composable
fun KolabTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KolabDarkColorScheme,
        typography = Typography,
        content = content
    )
}