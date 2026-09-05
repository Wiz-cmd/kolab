package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextSecondary

@Composable
fun KolabTopBar(
    location: String,
    onLocationClick: () -> Unit,
    onSearchClick: () -> Unit,
    title: String = "Kolab"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh.copy(alpha = 0.55f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("kolab_top_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Location Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, DividerBorder, RoundedCornerShape(20.dp))
                    .clickable(onClick = onLocationClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("location_picker_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Location",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Centered Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = (-0.5).sp
            )

            // Right Action
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .border(1.dp, DividerBorder, CircleShape)
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
