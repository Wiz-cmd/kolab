package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthStep
import com.example.ui.KolabViewModel
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextSecondary

@Composable
fun AccountCreatedScreen(viewModel: KolabViewModel) {
    var iconVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        iconVisible = true
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header brand
            Text(
                text = "Kolab",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Checkmark badge with spring scale animation
            AnimatedVisibility(
                visible = iconVisible,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(2.dp, DividerBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(3.dp, PureWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Title
            Text(
                text = "Account Created!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle
            Text(
                text = "Welcome to the creative elite. Your journey starts now.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1.2f))

            // Get Started Button
            Button(
                onClick = { viewModel.setAuthStep(AuthStep.AUTHENTICATED) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("get_started_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
