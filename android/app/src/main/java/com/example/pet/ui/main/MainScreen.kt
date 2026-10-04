package com.example.pet.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet.navigation.Screen

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()
    val backStackEntry by bottomNavController.currentBackStackEntryAsState()

    val currentScreen = backStackEntry?.destination?.route
        ?.let { route -> Screen.entries.firstOrNull { it.name == route } }
        ?: Screen.Feed

    val navigateToTab: (Screen) -> Unit = { screen ->
        bottomNavController.navigate(screen.name) {
            popUpTo(bottomNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val backToFeed = { navigateToTab(Screen.Feed) }

    BackHandler(enabled = currentScreen != Screen.Feed) {
        backToFeed()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BottomNavGraph(
            bottomNavController = bottomNavController,
            onBackToFeed = backToFeed,
            onCreateRequest = { navController.navigate(Screen.CreateRequest.name) },
            onAddPet = { navController.navigate(Screen.PetProfile.name) },
            modifier = Modifier.weight(1f)
        )

        BottomNavBar(
            currentScreen = currentScreen,
            onItemClick = navigateToTab
        )
    }
}