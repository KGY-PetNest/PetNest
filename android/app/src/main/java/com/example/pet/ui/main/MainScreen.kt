package com.example.pet.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet.navigation.Screen
import com.example.pet.ui.components.AdaptivePane

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()
    val backStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Feed.name,
            modifier = Modifier.weight(1f)
        ) {
            composable(Screen.Feed.name) {
                AdaptivePane {
                    FeedScreen(
                        onCreateClick = {
                            navController.navigate(Screen.CreateRequest.name)
                        }
                    )
                }
            }

            composable(Screen.Chat.name) {
                AdaptivePane {
                    ChatScreen()
                }
            }

            composable(Screen.Guide.name) {
                AdaptivePane {
                    GuideScreen()
                }
            }

            composable(Screen.Profile.name) {
                AdaptivePane {
                    ProfileScreen(
                        onAddPetClick = {
                            navController.navigate(Screen.PetProfile.name)
                        }
                    )
                }
            }
        }

        BottomNavBar(
            currentScreen = Screen.valueOf(currentRoute ?: Screen.Feed.name),
            onItemClick = { screen ->
                bottomNavController.navigate(screen.name) {
                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Feed, "Заявки", Icons.Default.List),
    BottomNavItem(Screen.Chat, "Чат", Icons.Default.ChatBubble),
    BottomNavItem(Screen.Guide, "Справочник", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Screen.Profile, "Профиль", Icons.Default.Person)
)

@Composable
private fun BottomNavBar(
    currentScreen: Screen,
    onItemClick: (Screen) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Column {
        HorizontalDivider(
            color = primary.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 4.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.height(64.dp)
        ) {
            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    selected = currentScreen == item.screen,
                    onClick = { onItemClick(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
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