package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun KolabBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("kolab_bottom_navigation_bar")
    ) {
        // Glassmorphic floating bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x332A2A2A),
                            Color(0x55141414),
                            Color(0xCC000000)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color(0x18FFFFFF),
                            Color(0x10FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                BottomNavItem(
                    tab = NavigationTab.HOME,
                    label = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    isSelected = currentTab == NavigationTab.HOME,
                    onClick = { onTabSelected(NavigationTab.HOME) },
                    testTag = "nav_tab_home"
                )

                // 2. Discover
                BottomNavItem(
                    tab = NavigationTab.DISCOVER,
                    label = "Discover",
                    selectedIcon = Icons.Filled.Explore,
                    unselectedIcon = Icons.Outlined.Explore,
                    isSelected = currentTab == NavigationTab.DISCOVER,
                    onClick = { onTabSelected(NavigationTab.DISCOVER) },
                    testTag = "nav_tab_discover"
                )

                // 3. CREATE (Prominent in the Middle!)
                CenterCreateNavItem(
                    isSelected = currentTab == NavigationTab.CREATE,
                    onClick = { onTabSelected(NavigationTab.CREATE) }
                )

                // 4. Places
                BottomNavItem(
                    tab = NavigationTab.PLACES,
                    label = "Places",
                    selectedIcon = Icons.Filled.Place,
                    unselectedIcon = Icons.Outlined.Place,
                    isSelected = currentTab == NavigationTab.PLACES,
                    onClick = { onTabSelected(NavigationTab.PLACES) },
                    testTag = "nav_tab_places"
                )

                // 5. Profile
                BottomNavItem(
                    tab = NavigationTab.PROFILE,
                    label = "Profile",
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                    isSelected = currentTab == NavigationTab.PROFILE,
                    onClick = { onTabSelected(NavigationTab.PROFILE) },
                    testTag = "nav_tab_profile"
                )
            }
        }
    }
}

@Composable
private fun CenterCreateNavItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "createScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("nav_tab_create"),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic glowing center circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isSelected) {
                            listOf(PureWhite, Color(0xFFE0E0E0))
                        } else {
                            listOf(
                                Color(0x35FFFFFF),
                                Color(0x15FFFFFF)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x80FFFFFF),
                            Color(0x30FFFFFF)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create",
                tint = if (isSelected) PureBlack else PureWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: NavigationTab,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) PureWhite else TextSecondary,
        animationSpec = tween(250),
        label = "iconTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "tabScale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = iconTint,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

