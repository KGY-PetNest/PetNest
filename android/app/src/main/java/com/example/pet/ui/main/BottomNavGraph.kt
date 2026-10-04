package com.example.pet.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet.navigation.Screen
import com.example.pet.ui.components.AdaptivePane

@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    onBackToFeed: () -> Unit,
    onCreateRequest: () -> Unit,
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = bottomNavController,
        startDestination = Screen.Feed.name,
        modifier = modifier
    ) {
        composable(Screen.Feed.name) {
            AdaptivePane {
                FeedScreen(onCreateClick = onCreateRequest)
            }
        }

        composable(Screen.Chat.name) {
            AdaptivePane {
                ChatScreen(onBack = onBackToFeed)
            }
        }

        composable(Screen.Guide.name) {
            AdaptivePane {
                GuideScreen(onBack = onBackToFeed)
            }
        }

        composable(Screen.Profile.name) {
            AdaptivePane {
                ProfileScreen(
                    onBack = onBackToFeed,
                    onAddPetClick = onAddPet
                )
            }
        }
    }
}