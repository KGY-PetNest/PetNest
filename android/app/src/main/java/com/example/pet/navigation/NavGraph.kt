package com.example.pet.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pet.data.MockData
import com.example.pet.data.UserRole
import com.example.pet.ui.responses.ResponsesScreen
import com.example.pet.ui.volunteerprofile.VolunteerProfileScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet.ui.components.BottomInsetsPane
import com.example.pet.ui.createrequest.CreateRequestScreen
import com.example.pet.ui.emailconfirm.EmailConfirmScreen
import com.example.pet.ui.login.LoginScreen
import com.example.pet.ui.main.MainScreen
import com.example.pet.ui.mappicker.MapPickerScreen
import com.example.pet.ui.petprofile.PetProfileScreen
import com.example.pet.ui.registration.RegistrationScreen
import com.example.pet.ui.welcome.WelcomeScreen

private const val PICKED_ADDRESS_KEY = "picked_address"


private const val NAV_DURATION = 320
private val defaultEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(SlideDirection.Start, tween(NAV_DURATION, easing = FastOutSlowInEasing)) +
            fadeIn(tween(NAV_DURATION / 2))
}
private val defaultExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        SlideDirection.Start,
        tween(NAV_DURATION, easing = FastOutSlowInEasing),
        targetOffset = { it / 4 }
    ) + fadeOut(tween(NAV_DURATION))
}
private val defaultPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        SlideDirection.End,
        tween(NAV_DURATION, easing = FastOutSlowInEasing),
        initialOffset = { it / 4 }
    ) + fadeIn(tween(NAV_DURATION))
}
private val defaultPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(SlideDirection.End, tween(NAV_DURATION, easing = FastOutSlowInEasing)) +
            fadeOut(tween(NAV_DURATION / 2))
}


private val sheetEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInVertically(tween(NAV_DURATION, easing = LinearOutSlowInEasing)) { it / 3 } +
            fadeIn(tween(NAV_DURATION))
}
private val sheetPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutVertically(tween(NAV_DURATION, easing = FastOutSlowInEasing)) { it / 3 } +
            fadeOut(tween(NAV_DURATION / 2))
}

private val mainEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(NAV_DURATION)) + scaleIn(tween(NAV_DURATION), initialScale = 0.96f)
}

private fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.name,
        modifier = modifier,
        enterTransition = defaultEnter,
        exitTransition = defaultExit,
        popEnterTransition = defaultPopEnter,
        popExitTransition = defaultPopExit
    ) {
        composable(Screen.Welcome.name) { entry ->
            BottomInsetsPane {
                WelcomeScreen(
                    onStart = {
                        entry.ifResumed {
                            navController.navigate(Screen.Login.name) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Login.name) { entry ->
            BottomInsetsPane {
                LoginScreen(
                    onRegisterClick = {
                        entry.ifResumed {
                            navController.navigate(Screen.Registration.name) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onSuccess = { role ->
                        entry.ifResumed {
                            navController.navigate(Routes.main(role)) {
                                popUpTo(Screen.Welcome.name) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Registration.name) { entry ->
            BottomInsetsPane {
                RegistrationScreen(
                    onLoginClick = {
                        entry.ifResumed {
                            navController.popBackStack(Screen.Login.name, inclusive = false)
                        }
                    },
                    onSuccess = {
                        entry.ifResumed {
                            navController.navigate(Screen.EmailConfirm.name) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.EmailConfirm.name) { entry ->
            BottomInsetsPane {
                EmailConfirmScreen(
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onSuccess = {
                        entry.ifResumed {
                            navController.popBackStack(Screen.Login.name, inclusive = false)
                        }
                    },
                    onResend = {
                        /* TODO: повторная отправка кода */
                    }
                )
            }
        }

        composable(
            route = Routes.MAIN,
            arguments = listOf(navArgument(Routes.ROLE_ARG) { type = NavType.StringType }),
            enterTransition = mainEnter
        ) { entry ->
            val role = entry.arguments?.getString(Routes.ROLE_ARG)
                ?.let { name -> UserRole.entries.firstOrNull { it.name == name } }
                ?: UserRole.Owner
            MainScreen(navController, role)
        }

        composable(
            route = Routes.RESPONSES,
            arguments = listOf(navArgument(Routes.REQUEST_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            val requestId = entry.arguments?.getString(Routes.REQUEST_ID_ARG).orEmpty()
            BottomInsetsPane {
                ResponsesScreen(
                    requestId = requestId,
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onVolunteerClick = { volunteerId ->
                        entry.ifResumed {
                            navController.navigate(Routes.volunteerProfile(volunteerId)) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.VOLUNTEER_PROFILE,
            arguments = listOf(navArgument(Routes.VOLUNTEER_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            val volunteer = entry.arguments?.getString(Routes.VOLUNTEER_ID_ARG)
                ?.let(MockData::volunteer)
                ?: MockData.volunteers.first()
            BottomInsetsPane {
                VolunteerProfileScreen(
                    volunteer = volunteer,
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onWrite = {
                        /* TODO: открыть чат с волонтёром */
                    }
                )
            }
        }

        composable(Screen.PetProfile.name) { entry ->
            BottomInsetsPane(includeIme = false) {
                PetProfileScreen(
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onSave = {
                        entry.ifResumed { navController.popBackStack() }
                    }
                )
            }
        }

        composable(
            route = Screen.CreateRequest.name,
            exitTransition = {
                if (targetState.destination.route == Screen.MapPicker.name) {
                    fadeOut(tween(NAV_DURATION))
                } else {
                    defaultExit(this)
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.MapPicker.name) {
                    fadeIn(tween(NAV_DURATION))
                } else {
                    defaultPopEnter(this)
                }
            }
        ) { entry ->
            val pickedAddress by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_ADDRESS_KEY, null)
                .collectAsState()

            BottomInsetsPane(includeIme = false) {
                CreateRequestScreen(
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onAddPet = {
                        entry.ifResumed {
                            navController.navigate(Screen.PetProfile.name) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onCreate = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    pickedAddress = pickedAddress,
                    onPickedAddressUsed = {
                        entry.savedStateHandle[PICKED_ADDRESS_KEY] = null
                    },
                    onPickOnMap = {
                        entry.ifResumed {
                            navController.navigate(Screen.MapPicker.name) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.MapPicker.name,
            enterTransition = sheetEnter,
            popExitTransition = sheetPopExit
        ) { entry ->
            BottomInsetsPane {
                MapPickerScreen(
                    onBack = {
                        entry.ifResumed { navController.popBackStack() }
                    },
                    onPicked = { address ->
                        entry.ifResumed {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(PICKED_ADDRESS_KEY, address)

                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}