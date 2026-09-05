package com.example.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.ui.AuthStep
import com.example.ui.KolabViewModel
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextSecondary

@Composable
fun SocialAuthScreen(
    provider: String,
    viewModel: KolabViewModel,
    inProgressText: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    var isStarting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
    }

    val badgeText = when (provider.lowercase()) {
        "google" -> "G"
        "apple" -> "\uF8FF"
        "instagram" -> "Ig"
        "twitter-x", "twitter" -> "X"
        else -> "@"
    }

    fun startProviderAuth() {
        if (isStarting) return
        isStarting = true
        viewModel.completeSocialSignIn(provider) { result ->
            if (isStarting) isStarting = false
            result.onSuccess { start ->
                val authUrl = start.authUrl
                if (!start.configured || authUrl.isNullOrBlank()) {
                    viewModel.showNotification("$provider sign-in isn't configured on the Kolab backend yet. Add client credentials then try again.")
                    viewModel.setAuthStep(AuthStep.LOGIN)
                    Toast.makeText(context, "$provider login not configured", Toast.LENGTH_SHORT).show()
                } else {
                    MainActivity.openAuthUrl(context, authUrl)
                }
            }.onFailure {
                isStarting = false
                viewModel.showNotification(it.message ?: "Unable to start $provider sign-in")
                viewModel.setAuthStep(AuthStep.LOGIN)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(400)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Brand Header
                Text(
                    text = "Kolab",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Modal Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(28.dp))
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, DividerBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (inProgressText != null) "..." else badgeText,
                                fontSize = if (inProgressText != null) 20.sp else 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Title
                        Text(
                            text = if (inProgressText != null) "Connecting..." else "Sign in with $provider",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description / Progress
                        if (inProgressText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = inProgressText
                                ?: when (provider.lowercase()) {
                                    "google" -> "Use your Google account to access your creative suite."
                                    "apple" -> "Securely sign in using your Apple ID."
                                    "instagram" -> "Link your Instagram to log in with your social profile."
                                    "twitter-x", "twitter" -> "Authorise Kolab via X to sign in instantly."
                                    else -> "Connect your account to continue."
                                },
                            fontSize = 15.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Continue Button
                        Button(
                            onClick = ::startProviderAuth,
                            enabled = inProgressText == null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("social_continue_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            )
                        ) {
                            Text(
                                text = if (inProgressText != null) "Please wait..." else "Continue with $provider",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Back to Login Button
                Row(
                    modifier = Modifier
                        .clickable { viewModel.setAuthStep(AuthStep.LOGIN) }
                        .padding(12.dp)
                        .testTag("back_to_login_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back to Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}