package com.example.pet.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet.data.UserRole
import com.example.pet.navigation.Routes
import com.example.pet.navigation.Screen

@Composable
fun MainScreen(navController: NavHostController, role: UserRole) {
    val bottomNavController = rememberNavController()
    val backStackEntry by bottomNavController.currentBackStackEntryAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val navigateOuter: (String) -> Unit = { route ->
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    BackHandler(enabled = currentScreen != Screen.Feed) {
        backToFeed()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BottomNavGraph(
            bottomNavController = bottomNavController,
            role = role,
            onBackToFeed = backToFeed,
            onCreateRequest = { navigateOuter(Screen.CreateRequest.name) },
            onAddPet = { navigateOuter(Screen.PetProfile.name) },
            onOpenResponses = { requestId -> navigateOuter(Routes.responses(requestId)) },
            onVolunteerRequestClick = { /* TODO: экран заявки для волонтёра + «Откликнуться» */ },
            modifier = Modifier.weight(1f)
        )

        BottomNavBar(
            currentScreen = currentScreen,
            onItemClick = navigateToTab
        )
    }
}