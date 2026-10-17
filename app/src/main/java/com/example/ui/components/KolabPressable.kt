package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * Kolab press feedback. Cards and controls dip slightly under the finger and
 * spring back on release — the same tactile language as the bottom bar. The
 * ripple is suppressed on purpose: the glass surfaces already carry their own
 * edge light, so a rectangular ripple would fight the design.
 *
 * Usage:
 *   Modifier.kolabClickable { onClick() }
 *   Modifier.kolabClickable(pressedScale = 0.98f, enabled = enabled) { ... }
 */
@Composable
fun Modifier.kolabClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "kolabPressScale"
    )
    return this
        .scale(pressScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
