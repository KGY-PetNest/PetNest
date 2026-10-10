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

class MainActions(
    val onBackToFeed: () -> Unit,
    val onCreateRequest: () -> Unit,
    val onOpenResponses: (String) -> Unit,
    val onOpenRequestDetails: (String) -> Unit,
    val onPickVolunteerLocation: () -> Unit,
    val onAddPet: () -> Unit,
    val onEditPet: (String) -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenReviews: (String) -> Unit,
    val onOpenChat: (String) -> Unit
)

@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    role: UserRole,
    actions: MainActions,
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
                        onCreateClick = actions.onCreateRequest,
                        onRequestClick = actions.onOpenResponses
                    )
                    UserRole.Volunteer -> VolunteerFeedScreen(
                        onRequestClick = actions.onOpenRequestDetails,
                        onPickLocationOnMap = actions.onPickVolunteerLocation,
                        onOpenChat = actions.onOpenChat
                    )
                }
            }
        }

        composable(Screen.Chat.name) {
            AdaptivePane {
                ChatScreen(
                    role = role,
                    onOpenChat = actions.onOpenChat
                )
            }
        }

        composable(Screen.Guide.name) {
            AdaptivePane {
                GuideScreen()
            }
        }

        composable(Screen.Profile.name) {
            AdaptivePane {
                when (role) {
                    UserRole.Owner -> ProfileScreen(
                        onOpenSettings = actions.onOpenSettings,
                        onAddPetClick = actions.onAddPet,
                        onPetClick = actions.onEditPet
                    )
                    UserRole.Volunteer -> VolunteerProfileScreen(
                        volunteerId = MockData.CURRENT_VOLUNTEER_ID,
                        onAllReviews = actions.onOpenReviews,
                        onOpenSettings = actions.onOpenSettings
                    )
                }
            }
        }
    }
}
