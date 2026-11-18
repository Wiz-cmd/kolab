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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KolabViewModel
import com.example.ui.components.kolabClickable
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextSecondary

@Composable
fun LoginOtpScreen(viewModel: KolabViewModel) {
    val challenge by viewModel.loginChallenge.collectAsState()
    val busy by viewModel.isLoggingIn.collectAsState()
    var code by remember(challenge?.challengeId) { mutableStateOf("") }

    val submit: (String) -> Unit = { value ->
        if (!busy && value.length == 6) viewModel.verifyLoginOtp(value)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101010), Color(0xFF060606)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextSecondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(24.dp)
                    .kolabClickable { viewModel.cancelLoginOtp() }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Verify it's you",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We sent a 6-digit code to ${challenge?.destination ?: "your account"} via ${challenge?.channel ?: "email"}. Enter it below to finish signing in.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .border(1.dp, DividerBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (index < code.length) code[index].toString() else "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            val digits = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "del")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                digits.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { key ->
                            when (key) {
                                "" -> Spacer(modifier = Modifier.size(58.dp))
                                "del" -> Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .clickable { code = code.dropLast(1) },
                                    contentAlignment = Alignment.Center
                                ) { Text("⌫", fontSize = 20.sp, color = TextSecondary) }
                                else -> Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh)
                                        .border(1.dp, DividerBorder, CircleShape)
                                        .kolabClickable {
                                            if (code.length < 6) {
                                                val next = code + key
                                                code = next
                                                if (next.length == 6) submit(next)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PureWhite)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (busy) "Verifying…" else "Resend code",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                modifier = Modifier.kolabClickable { viewModel.resendLoginOtp() }
            )
        }
    }
}
