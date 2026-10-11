package com.example.pet.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.navigation.Screen

private const val BADGE_MAX = 99

private data class BottomNavItem(
    val screen: Screen,
    @param:StringRes val label: Int,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Feed, R.string.common_tab_requests, Icons.AutoMirrored.Filled.List),
    BottomNavItem(Screen.Chat, R.string.common_tab_chat, Icons.Default.ChatBubble),
    BottomNavItem(Screen.Handbook, R.string.common_tab_handbook, Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Screen.Profile, R.string.common_tab_profile, Icons.Default.Person)
)

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    unreadChats: Int = 0
) {
    val primary = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        HorizontalDivider(
            color = primary.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    selected = currentScreen == item.screen,
                    onClick = { onItemClick(item.screen) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (item.screen == Screen.Chat && unreadChats > 0) {
                                    Badge(
                                        containerColor = primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(if (unreadChats > BADGE_MAX) "$BADGE_MAX+" else unreadChats.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.label),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primary,
                        selectedTextColor = primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
