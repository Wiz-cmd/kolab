package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KolabViewModel
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollabSheet(
    viewModel: KolabViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedMode by remember { mutableStateOf("collab_call") } // "collab_call", "studio_space", "portfolio_item"
    var titleOrLocation by remember { mutableStateOf("Braamfontein, JHB") }
    var description by remember { mutableStateOf("") }
    var customImageUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = PureBlack.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("create_collab_modal_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Create or Collab",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreationModeChip(
                    label = "Collab Call",
                    icon = Icons.Default.Handshake,
                    isSelected = selectedMode == "collab_call",
                    onClick = { selectedMode = "collab_call" }
                )
                CreationModeChip(
                    label = "Studio Space",
                    icon = Icons.Default.Domain,
                    isSelected = selectedMode == "studio_space",
                    onClick = { selectedMode = "studio_space" }
                )
                CreationModeChip(
                    label = "Portfolio",
                    icon = Icons.Default.PhotoLibrary,
                    isSelected = selectedMode == "portfolio_item",
                    onClick = { selectedMode = "portfolio_item" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedMode == "portfolio_item") {
                Text(
                    text = "Project Image URL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customImageUrl,
                    onValueChange = { customImageUrl = it },
                    placeholder = { Text("https://images.unsplash.com/...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("portfolio_image_url_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerHigh,
                        unfocusedContainerColor = SurfaceContainerHigh,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DividerBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val img = if (customImageUrl.isNotBlank()) customImageUrl else "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80"
                        viewModel.addPortfolioImage(img)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_portfolio_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    )
                ) {
                    Text(
                        text = "Add to Portfolio Grid",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack
                    )
                }
            } else {
                // Location / Area
                Text(
                    text = if (selectedMode == "collab_call") "Location / Neighborhood" else "Studio Location",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = titleOrLocation,
                    onValueChange = { titleOrLocation = it },
                    placeholder = { Text("e.g. Rosebank, Johannesburg", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerHigh,
                        unfocusedContainerColor = SurfaceContainerHigh,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DividerBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Post Content
                Text(
                    text = if (selectedMode == "collab_call") "What are you looking for?" else "Studio Description & Specs",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            if (selectedMode == "collab_call")
                                "e.g. Looking for a stylist and MUA for a 90s aesthetic editorial shoot..."
                            else
                                "e.g. 120sqm cyclorama wall, Profoto lighting kits, makeup vanity available...",
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(110.dp).testTag("create_post_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerHigh,
                        unfocusedContainerColor = SurfaceContainerHigh,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DividerBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val author = viewModel.currentUserName.value
                        val isStudio = selectedMode == "studio_space"
                        val sampleImg = if (isStudio) {
                            "https://lh3.googleusercontent.com/aida-public/AB6AXuBMHILIDiYM6ubXnuiaYUJCSWANqjRcdDQ9nVj4nzezAy7kAIPqxthI2FYf177kCMoxHI6W24qjt6L2zVgtneKwZJbzZPhQ3PppXW4r_vwgjXVxc-Y_aR0l8uIBHhivFEf8F56AmGlhIWOCtrJ9Wl_9dPDp1Itcq_h4boVkGDCyHE76bX8e16rcKGzklTRX4NpYGLleVpma9NspvF0wSneS_KjW7mEAcobO4St9Vi06oPn2LBZ5X8yoSw"
                        } else {
                            "https://lh3.googleusercontent.com/aida-public/AB6AXuCmEPvxSoMv6qUa5LCQDMZZ59iI32DhpnGMXEg3uVTgDJYubDHZ08-UKW7tjmjblYyyervsXds6-xiaY8i6guU6CkndKhQppRVm8I_lfgdaxRIkBbOrAVG9PSTGruyxExCZsmEvTElI8ZEjDT67IL603EdyZAdXlrFkelskjhzsyeQq-ai9M3nzIWY7-yJ5opy1EqW0H6nkPB7UKBFgi7dcLmYhDN9cH-SFIlLzb8TrVBVu-_3Et-ebDg"
                        }
                        viewModel.createNewPost(
                            author = author,
                            location = titleOrLocation.ifBlank { "Johannesburg" },
                            description = description.ifBlank { "Excited to collaborate with local creatives on this upcoming series." },
                            imageUrl = sampleImg,
                            isStudio = isStudio
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_post_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    )
                ) {
                    Text(
                        text = "Post to Creative Network",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CreationModeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(PureWhite, Color(0xFFEEEEEE))
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0x30FFFFFF), Color(0x12FFFFFF))
        )
    }

    val borderBrush = if (isSelected) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xDDFFFFFF))
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0x50FFFFFF), Color(0x15FFFFFF))
        )
    }

    val textColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) PureBlack else PureWhite,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "modeTextColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
