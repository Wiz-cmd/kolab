package com.example.ui.profile

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.security.AppLockManager
import com.example.data.security.BiometricAuth
import com.example.ui.KolabViewModel
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SecurityScreen(
    viewModel: KolabViewModel,
    onBack: () -> Unit
) {
    val securityBusy by viewModel.securityBusy.collectAsState()
    val context = LocalContext.current as FragmentActivity

    var showChangePassword by remember { mutableStateOf(false) }
    var showTwoFactor by remember { mutableStateOf(false) }
    var twoFactorSecret by remember { mutableStateOf<String?>(null) }
    var twoFactorOtpUrl by remember { mutableStateOf<String?>(null) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var twoFactorCode by remember { mutableStateOf("") }
    var showLogoutAllConfirm by remember { mutableStateOf(false) }

    val appLockManager = remember { AppLockManager(context) }
    var appLockEnabled by remember { mutableStateOf(appLockManager.isEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, DividerBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack)
                    .testTag("security_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Security & Privacy",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (securityBusy) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Change password
            SecurityRow(
                icon = { Icon(Icons.Default.Key, null, tint = PureWhite, modifier = Modifier.size(20.dp)) },
                title = "Change password",
                subtitle = "Update the password for your account",
                onClick = { showChangePassword = true }
            )

            // Two-factor authentication
            SecurityRow(
                icon = { Icon(Icons.Default.Shield, null, tint = PureWhite, modifier = Modifier.size(20.dp)) },
                title = "Two-factor authentication",
                subtitle = if (twoFactorEnabled) "Enabled — 6-digit TOTP code required" else "Add an authenticator app for extra protection",
                onClick = {
                    val opening = !showTwoFactor
                    showTwoFactor = opening
                    if (opening && twoFactorSecret == null && !twoFactorEnabled) {
                        viewModel.setup2FA { result ->
                            result.onSuccess { setup ->
                                twoFactorSecret = setup.secret
                                twoFactorOtpUrl = setup.otpauthUrl
                            }.onFailure {
                                viewModel.showNotification(it.message ?: "Unable to set up two-factor authentication")
                            }
                        }
                    }
                }
            )

            if (showTwoFactor) {
                TwoFactorPanel(
                    twoFactorEnabled = twoFactorEnabled,
                    secret = twoFactorSecret,
                    otpUrl = twoFactorOtpUrl,
                    code = twoFactorCode,
                    onCodeChange = { twoFactorCode = it },
                    onEnable = {
                        viewModel.enable2FA(twoFactorCode.trim()) { result ->
                            result.onSuccess {
                                twoFactorEnabled = true
                                twoFactorCode = ""
                                viewModel.showNotification("Two-factor authentication enabled")
                            }.onFailure {
                                twoFactorCode = ""
                                viewModel.showNotification(it.message ?: "Invalid code. Try again.")
                            }
                        }
                    },
                    onDisable = {
                        viewModel.disable2FA(twoFactorCode.trim()) { result ->
                            result.onSuccess {
                                twoFactorEnabled = false
                                twoFactorSecret = null
                                twoFactorOtpUrl = null
                                twoFactorCode = ""
                                viewModel.showNotification("Two-factor authentication disabled")
                            }.onFailure {
                                twoFactorCode = ""
                                viewModel.showNotification(it.message ?: "Invalid code. Try again.")
                            }
                        }
                    }
                )
            }

            // App lock (biometric / device credential)
            SecurityRow(
                icon = { Icon(Icons.Default.PhonelinkLock, null, tint = PureWhite, modifier = Modifier.size(20.dp)) },
                title = "App lock",
                subtitle = if (appLockEnabled) "Require fingerprint or device credential to open Kolab" else "Lock Kolab behind your fingerprint or device PIN",
                trailing = {
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                BiometricAuth.authenticate(
                                    context,
                                    title = "Enable app lock",
                                    subtitle = "Confirm your identity to turn on app lock",
                                    onSuccess = {
                                        appLockManager.setEnabled(true)
                                        appLockEnabled = true
                                    },
                                    onError = { _, message ->
                                        viewModel.showNotification(message.ifBlank { "Biometrics unavailable" })
                                    }
                                )
                            } else {
                                appLockManager.setEnabled(false)
                                appLockEnabled = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = TextPrimary,
                            uncheckedTrackColor = SurfaceContainerHigh
                        ),
                        modifier = Modifier.testTag("app_lock_toggle")
                    )
                }
            )

            // E2EE status
            SecurityRow(
                icon = { Icon(Icons.Default.Lock, null, tint = PureWhite, modifier = Modifier.size(20.dp)) },
                title = "End-to-end encrypted chat",
                subtitle = "Messages are encrypted on your device before sending",
                onClick = { viewModel.showNotification("Chats use AES-256-GCM via ECDH-negotiated per-conversation keys") }
            )

            // Log out of all devices
            SecurityRow(
                icon = { Icon(Icons.Default.PhonelinkLock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                title = "Log out of all devices",
                subtitle = "Revoke sessions on every phone and browser",
                onClick = { showLogoutAllConfirm = true }
            )
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            busy = securityBusy,
            onDismiss = { showChangePassword = false },
            onSubmit = { current, next ->
                viewModel.changePassword(current, next) { result ->
                    result.onSuccess {
                        showChangePassword = false
                        viewModel.showNotification("Password updated")
                    }.onFailure {
                        viewModel.showNotification(it.message ?: "Unable to change password")
                    }
                }
            }
        )
    }

    if (showLogoutAllConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutAllConfirm = false },
            containerColor = CardBackground,
            title = { Text("Log out everywhere?", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This revokes access from every device. You will be signed out of this session too and will need to sign in again.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutAllConfirm = false
                        viewModel.logoutAllDevices { result ->
                            result.onSuccess {
                                viewModel.showNotification("Signed out of all devices")
                                viewModel.signOut()
                            }.onFailure {
                                viewModel.showNotification(it.message ?: "Unable to revoke devices")
                            }
                        }
                    }
                ) {
                    Text("Log out all", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutAllConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SecurityRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

@Composable
private fun TwoFactorPanel(
    twoFactorEnabled: Boolean,
    secret: String?,
    otpUrl: String?,
    code: String,
    onCodeChange: (String) -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, DividerBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (twoFactorEnabled) {
            Text(
                text = "Two-factor authentication is on.",
                color = TextPrimary,
                fontSize = 13.sp
            )
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("6-digit code", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureBlack,
                    unfocusedContainerColor = PureBlack,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = DividerBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )
            Button(
                onClick = onDisable,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text("Disable 2FA", fontWeight = FontWeight.Bold, color = PureBlack)
            }
        } else if (secret != null) {
            Text(
                text = "Scan or enter this secret in your authenticator app (Google Authenticator, Authy...).",
                color = TextPrimary,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureBlack)
                    .border(1.dp, DividerBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = otpUrl ?: secret,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 4
                )
            }
            Text(
                text = "Manual code: $secret",
                color = TextPrimary,
                fontSize = 12.sp
            )
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("6-digit code", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureBlack,
                    unfocusedContainerColor = PureBlack,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = DividerBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )
            Button(
                onClick = onEnable,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text("Enable 2FA", fontWeight = FontWeight.Bold, color = PureBlack)
            }
        } else {
            Text(
                text = "Preparing setup...",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirmText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Change password", color = PureWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current password", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = passwordColors()
                )
                OutlinedTextField(
                    value = next,
                    onValueChange = { next = it },
                    label = { Text("New password", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = passwordColors()
                )
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    label = { Text("Confirm new password", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = passwordColors()
                )
                if (error != null) {
                    Text(error!!, color = Color(0xFFFF6B6B), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        current.isBlank() || next.isBlank() -> error = "Fill in all fields"
                        next.length < 8 -> error = "New password must be at least 8 characters"
                        next != confirmText -> error = "New passwords do not match"
                        else -> onSubmit(current, next)
                    }
                },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Update", fontWeight = FontWeight.Bold, color = PureBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun passwordColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PureBlack,
    unfocusedContainerColor = PureBlack,
    focusedBorderColor = PureWhite,
    unfocusedBorderColor = DividerBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite
)