package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Theme state read by the colour accessors below. KolabTheme updates this
 * before content composes, so every existing `PureWhite` / `CardBackground`
 * reference automatically resolves to the right palette for the active theme.
 */
object KolabThemeState {
    var isLight by mutableStateOf(false)
}

private val DarkSurfaceColor = Color(0x0FFFFFFF)
private val DarkCard = Color(0x17FFFFFF)
private val DarkSurfaceHigh = Color(0x24FFFFFF)
private val DarkSurfaceHighest = Color(0x33FFFFFF)
private val DarkDivider = Color(0x38FFFFFF)
private val DarkOutline = Color(0x4DFFFFFF)

// Surfaces
val DarkSurface: Color get() = if (KolabThemeState.isLight) Color(0xFFFFFFFF) else DarkSurfaceColor
val CardBackground: Color get() = if (KolabThemeState.isLight) Color(0xFFFFFFFF) else DarkCard
val SurfaceContainerHigh: Color get() = if (KolabThemeState.isLight) Color(0xFFF0F2F6) else DarkSurfaceHigh
val SurfaceContainerHighest: Color get() = if (KolabThemeState.isLight) Color(0xFFE6EAF1) else DarkSurfaceHighest
val DividerBorder: Color get() = if (KolabThemeState.isLight) Color(0xFFD2D7E0) else DarkDivider
val OutlineVariant: Color get() = if (KolabThemeState.isLight) Color(0xFFB8BEC9) else DarkOutline

// Content / on-surface colours. In light mode "PureWhite" becomes ink and
// "PureBlack" becomes paper so buttons and selected pills keep contrast.
val PureWhite: Color get() = if (KolabThemeState.isLight) Color(0xFF15171A) else Color(0xFFFFFFFF)
val PureBlack: Color get() = if (KolabThemeState.isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
val OffWhite: Color get() = if (KolabThemeState.isLight) Color(0xFF2A2E36) else Color(0xFFF5F5F5)
val TextPrimary: Color get() = if (KolabThemeState.isLight) Color(0xFF1A1D24) else Color(0xFFE2E2E2)
val TextSecondary: Color get() = if (KolabThemeState.isLight) Color(0xFF555B68) else Color(0xFFA0A0A0)
val TextMuted: Color get() = if (KolabThemeState.isLight) Color(0xFF7A818F) else Color(0xFF6E6E6E)

val StarGold = Color(0xFFFFD700)
val TrendGreen = Color(0xFF2E9E5B)
val ErrorRed = Color(0xFFD93025)

// Glass backdrop — deep black with a cold sheen drifting from the top
val GlassBaseTop = Color(0xFF313131)
val GlassBaseMid = Color(0xFF151515)
val GlassBaseBottom = PureBlack
val GlassSheen = Color(0x1AFFFFFF)
val GlassSpecular = Color(0x66FFFFFF)

// Fixed light-theme base (used by Theme.kt)
val LightBackground = Color(0xFFEDF0F5)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFF0F2F6)
val LightDivider = Color(0xFFD2D7E0)
val LightTextPrimary = Color(0xFF1A1D24)
val LightTextSecondary = Color(0xFF555B68)
