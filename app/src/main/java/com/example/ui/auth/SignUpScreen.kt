package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthStep
import com.example.ui.KolabViewModel
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun SignUpScreen(viewModel: KolabViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
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
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = tween(600)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Brand Title
                Text(
                    text = "Kolab",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Join the creative elite",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Form Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBackground)
                        .border(1.dp, DividerBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Full Name
                        Text(
                            text = "Full Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("Jane Doe", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_fullname_input"),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Name Icon",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = DividerBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Address
                        Text(
                            text = "Email Address",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("jane@studio.com", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_email_input"),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = DividerBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password
                        Text(
                            text = "Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_password_input"),
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Icon",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = DividerBorder,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.signUp(fullName, email, password) }
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Create Account Button
                        Button(
                            onClick = { viewModel.signUp(fullName, email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("create_account_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            )
                        ) {
                            Text(
                                text = "Create Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // OR Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = DividerBorder,
                                thickness = 1.dp
                            )
                            Text(
                                text = "OR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = DividerBorder,
                                thickness = 1.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Continue with Apple
                        SocialAuthButton(
                            label = "Continue with Apple",
                            iconType = "apple",
                            onClick = { viewModel.setAuthStep(AuthStep.APPLE_AUTH_SHEET) },
                            testTag = "signup_with_apple_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Continue with Google
                        SocialAuthButton(
                            label = "Continue with Google",
                            iconType = "google",
                            onClick = { viewModel.setAuthStep(AuthStep.GOOGLE_AUTH_SHEET) },
                            testTag = "signup_with_google_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Continue with Instagram
                        SocialAuthButton(
                            label = "Continue with Instagram",
                            iconType = "instagram",
                            onClick = { viewModel.setAuthStep(AuthStep.INSTAGRAM_AUTH_SHEET) },
                            testTag = "signup_with_instagram_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Continue with X
                        SocialAuthButton(
                            label = "Continue with X",
                            iconType = "x",
                            onClick = { viewModel.setAuthStep(AuthStep.X_AUTH_SHEET) },
                            testTag = "signup_with_x_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Sign In Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier
                            .clickable { viewModel.setAuthStep(AuthStep.LOGIN) }
                            .testTag("nav_to_sign_in_button")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
