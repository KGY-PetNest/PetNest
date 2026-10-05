package com.example.pet.ui.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet.data.MockData
import com.example.pet.data.UserRole
import com.example.pet.navigation.Screen
import com.example.pet.ui.components.AdaptivePane
import com.example.pet.ui.volunteerprofile.VolunteerProfileScreen

private const val TAB_FADE_MS = 200

@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    role: UserRole,
    onBackToFeed: () -> Unit,
    onCreateRequest: () -> Unit,
    onAddPet: () -> Unit,
    onOpenResponses: (String) -> Unit,
    onVolunteerRequestClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = bottomNavController,
        startDestination = Screen.Feed.name,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TAB_FADE_MS)) },
        exitTransition = { fadeOut(tween(TAB_FADE_MS)) },
        popEnterTransition = { fadeIn(tween(TAB_FADE_MS)) },
        popExitTransition = { fadeOut(tween(TAB_FADE_MS)) }
    ) {
        composable(Screen.Feed.name) {
            AdaptivePane {
                when (role) {
                    UserRole.Owner -> FeedScreen(
                        onCreateClick = onCreateRequest,
                        onRequestClick = onOpenResponses
                    )
                    UserRole.Volunteer -> VolunteerFeedScreen(
                        onRequestClick = onVolunteerRequestClick
                    )
                }
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
                when (role) {
                    UserRole.Owner -> ProfileScreen(
                        onBack = onBackToFeed,
                        onAddPetClick = onAddPet
                    )
                    UserRole.Volunteer -> VolunteerProfileScreen(
                        // TODO: реальный профиль текущего пользователя
                        volunteer = MockData.currentVolunteer,
                        onBack = onBackToFeed
                    )
                }
            }
        }
    }
}