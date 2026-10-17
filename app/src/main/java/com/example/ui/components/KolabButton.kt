package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

/**
 * The Kolab primary action button, with a built-in in-flight state.
 *
 * While [loading], the label fades out and a spinner takes its place — the
 * button keeps its size (the label stays laid out underneath), so there is no
 * jump, and it is disabled against double taps. Fits the Noir Glass look by
 * default but every colour can be overridden.
 */
@Composable
fun KolabPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = PureWhite,
    contentColor: Color = PureBlack,
    height: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    fontSize: Int = 14
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (loading) 0f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "kolabButtonContentAlpha"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        modifier = modifier.height(height)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Always laid out so the button width never changes while loading.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            }
        }
    }
}
