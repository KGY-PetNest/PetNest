package com.example.pet.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet.data.AppContainer
import com.example.pet.data.UserRole
import com.example.pet.navigation.Routes
import com.example.pet.navigation.Screen

@Composable
fun MainScreen(
    navController: NavHostController,
    role: UserRole
) {
    val bottomNavController = rememberNavController()
    val backStackEntry by bottomNavController.currentBackStackEntryAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val chats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val unreadChats = chats.sumOf { it.unreadCount }

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

    val isResumed = { lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED }
    val navigateOuter: (String) -> Unit = { route ->
        if (isResumed()) {
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
            actions = MainActions(
                onBackToFeed = backToFeed,
                onCreateRequest = { navigateOuter(Routes.createRequest()) },
                onOpenResponses = { navigateOuter(Routes.responses(it)) },
                onOpenRequestDetails = { navigateOuter(Routes.requestDetails(it)) },
                onPickVolunteerLocation = { navigateOuter(Routes.mapPicker(forVolunteer = true)) },
                onAddPet = { navigateOuter(Routes.petProfile()) },
                onEditPet = { navigateOuter(Routes.petProfile(it)) },
                onOpenSettings = { navigateOuter(Routes.settings(role)) },
                onOpenReviews = { navigateOuter(Routes.reviews(it)) },
                onOpenChat = { navigateOuter(Routes.conversation(it, role)) }
            ),
            modifier = Modifier.weight(1f)
        )

        BottomNavBar(
            currentScreen = currentScreen,
            onItemClick = navigateToTab,
            unreadChats = unreadChats
        )
    }
}
