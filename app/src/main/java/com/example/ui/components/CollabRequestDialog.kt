package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CollabRequest
import com.example.data.CreativeProfile
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
fun CollabRequestBottomSheet(
    creative: CreativeProfile,
    onDismiss: () -> Unit,
    onSubmit: (CollabRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var projectTitle by remember { mutableStateOf("") }
    var roleRequired by remember { mutableStateOf(creative.role) }
    var proposedDate by remember { mutableStateOf("This Weekend") }
    var budget by remember { mutableStateOf(creative.rate) }
    var notes by remember { mutableStateOf("") }

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
                .testTag("collab_request_bottom_sheet")
        ) {
            // Header with target creative info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = creative.imageUrl,
                        contentDescription = creative.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, DividerBorder, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Collaborate with",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = creative.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Project Title Input
            Text(
                text = "Project Name / Concept",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = projectTitle,
                onValueChange = { projectTitle = it },
                placeholder = { Text("e.g. High-Fashion Urban Lookbook", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().testTag("collab_project_title_input"),
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

            // Proposed Date & Budget Row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proposed Date",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = proposedDate,
                        onValueChange = { proposedDate = it },
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
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Offer Budget",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
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
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Project Details / Pitch
            Text(
                text = "Concept Pitch & Location Details",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Describe the mood, wardrobe, lighting setup, or studio location...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("collab_notes_input"),
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

            // Submit Button
            Button(
                onClick = {
                    onSubmit(
                        CollabRequest(
                            creatorName = creative.name,
                            projectTitle = projectTitle.ifBlank { "Editorial Collab" },
                            roleRequired = roleRequired,
                            proposedDate = proposedDate,
                            budgetZar = budget,
                            notes = notes
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_collab_request_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text(
                    text = "Send Collaboration Request",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
