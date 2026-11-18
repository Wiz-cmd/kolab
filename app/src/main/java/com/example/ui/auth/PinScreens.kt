package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.AppLockManager
import com.example.ui.components.kolabClickable
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextSecondary

private val PinBackground = Brush.verticalGradient(
    listOf(Color(0xFF101010), Color(0xFF060606))
)

@Composable
private fun PinShell(
    title: String,
    subtitle: String,
    error: String?,
    pin: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    footer: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(PinBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .border(1.dp, DividerBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = PureWhite, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(AppLockManager.PIN_LENGTH) { index ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (index < pin.length) PureWhite else Color(0x33FFFFFF))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error ?: " ",
                fontSize = 12.sp,
                color = Color(0xFFFF8A80),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))

            PinKeypad(onDigit = onDigit, onDelete = onDelete)
            Spacer(modifier = Modifier.height(12.dp))
            footer()
        }
    }
}

@Composable
private fun PinKeypad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "del")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(modifier = Modifier.size(62.dp))
                        "del" -> Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⌫", fontSize = 22.sp, color = TextSecondary)
                        }
                        else -> Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, DividerBorder, CircleShape)
                                .kolabClickable { onDigit(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, color = PureWhite)
                        }
                    }
                }
            }
        }
    }
}

/** First-run PIN setup shown immediately after the first successful sign-in. */
@Composable
fun PinSetupOverlay(
    onComplete: (String) -> Unit,
    onSkip: () -> Unit
) {
    var first by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var confirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val onDigit: (String) -> Unit = { digit ->
        error = null
        if (!confirming) {
            if (first.length < AppLockManager.PIN_LENGTH) {
                first += digit
                if (first.length == AppLockManager.PIN_LENGTH) confirming = true
            }
        } else if (confirm.length < AppLockManager.PIN_LENGTH) {
            confirm += digit
            if (confirm.length == AppLockManager.PIN_LENGTH) {
                if (confirm == first) onComplete(first)
                else {
                    error = "PINs don't match. Try again."
                    first = ""
                    confirm = ""
                    confirming = false
                }
            }
        }
    }
    val onDelete: () -> Unit = {
        error = null
        if (confirming && confirm.isNotEmpty()) confirm = confirm.dropLast(1)
        else if (!confirming && first.isNotEmpty()) first = first.dropLast(1)
    }

    PinShell(
        title = if (confirming) "Confirm your PIN" else "Create your login PIN",
        subtitle = if (confirming) "Enter the same 6 digits once more" else "This keeps your Kolab account private on this device.",
        error = error,
        pin = if (confirming) confirm else first,
        onDigit = onDigit,
        onDelete = onDelete,
        footer = {
            Text(
                text = "Skip for now",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.kolabClickable(onClick = onSkip)
            )
        }
    )
}

/** PIN gate shown whenever the locked app is resumed. */
@Composable
fun PinLockOverlay(
    onVerify: (String) -> Boolean,
    onUnlocked: () -> Unit,
    onBiometric: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val onDigit: (String) -> Unit = { digit ->
        if (pin.length < AppLockManager.PIN_LENGTH) {
            error = null
            pin += digit
            if (pin.length == AppLockManager.PIN_LENGTH) {
                if (onVerify(pin)) onUnlocked()
                else {
                    error = "Incorrect PIN. Try again."
                    pin = ""
                }
            }
        }
    }
    val onDelete: () -> Unit = { error = null; pin = pin.dropLast(1) }

    PinShell(
        title = "Kolab is locked",
        subtitle = "Enter your 6-digit login PIN to continue.",
        error = error,
        pin = pin,
        onDigit = onDigit,
        onDelete = onDelete,
        footer = {
            Text(
                text = "Use device unlock instead",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.kolabClickable(onClick = onBiometric)
            )
        }
    )
}
