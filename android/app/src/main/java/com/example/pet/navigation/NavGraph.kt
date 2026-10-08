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
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.GeoPoint
import com.example.pet.data.SavedLocation
import com.example.pet.data.UserRole
import com.example.pet.notifications.ChatNotifier
import com.example.pet.notifications.PendingChatLink
import com.example.pet.notifications.PushRegistrar
import com.example.pet.ui.appguide.AppGuideScreen
import com.example.pet.ui.chat.AttachmentViewerScreen
import com.example.pet.ui.chat.ChatDrafts
import com.example.pet.ui.chat.ChatInfoScreen
import com.example.pet.ui.chat.ConversationScreen
import com.example.pet.ui.components.BottomInsetsPane
import com.example.pet.ui.createrequest.CreateRequestScreen
import com.example.pet.ui.editprofile.EditProfileScreen
import com.example.pet.ui.emailconfirm.EmailConfirmScreen
import com.example.pet.ui.login.LoginScreen
import com.example.pet.ui.main.MainScreen
import com.example.pet.ui.mappicker.MapPickerScreen
import com.example.pet.ui.password.ChangePasswordScreen
import com.example.pet.ui.password.ForgotPasswordScreen
import com.example.pet.ui.password.ResetPasswordScreen
import com.example.pet.ui.petprofile.PetProfileScreen
import com.example.pet.ui.registration.RegistrationScreen
import com.example.pet.ui.requestdetails.RequestDetailsScreen
import com.example.pet.ui.responses.ResponsesScreen
import com.example.pet.ui.reviews.ReviewsScreen
import com.example.pet.ui.settings.SettingsScreen
import com.example.pet.ui.volunteerprofile.VolunteerProfileScreen
import com.example.pet.ui.welcome.WelcomeScreen

private const val PICKED_ADDRESS_KEY = "picked_address"
private const val ADDED_PET_KEY = "added_pet"
private const val PICKED_POINT_KEY = "picked_point"
private const val MAP_START_KEY = "map_start"


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

private fun NavBackStackEntry.ifTop(navController: NavHostController, action: () -> Unit) {
    if (navController.currentBackStackEntry?.id == id) action()
}

private fun entryRouteFor(role: UserRole): String =
    if (AppContainer.settings.guideSeen(role)) Routes.main(role) else Routes.appGuide(role, firstRun = true)

private fun NavBackStackEntry.stringArg(name: String): String? = arguments?.getString(name)

private fun NavBackStackEntry.roleArg(): UserRole =
    stringArg(Routes.ROLE_ARG)
        ?.let { name -> UserRole.entries.firstOrNull { it.name == name } }
        ?: UserRole.Owner

private val SignedOutRoutes = setOf(
    Screen.Welcome.name,
    Screen.Login.name,
    Screen.Registration.name,
    Screen.EmailConfirm.name,
    Screen.ForgotPassword.name,
    Routes.RESET_CODE,
    Screen.ResetPassword.name
)

private fun NavHostController.logout(context: Context) {
    PushRegistrar.signOut(context)
    ChatNotifier.cancelAll(context)
    ChatDrafts.clear()
    PendingChatLink.consume()
    AppContainer.settings.setSessionRole(null)
    AppContainer.settings.clearVolunteerLocation()
    navigate(Screen.Login.name) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.openLogin() {
    if (!popBackStack(Screen.Login.name, inclusive = false)) {
        navigate(Screen.Login.name) {
            popUpTo(Screen.Welcome.name)
            launchSingleTop = true
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingLink by PendingChatLink.link.collectAsState()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    LaunchedEffect(pendingLink, currentRoute) {
        val link = pendingLink ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute in SignedOutRoutes) return@LaunchedEffect
        if (currentRoute == Routes.APP_GUIDE && currentEntry?.arguments?.getBoolean(Routes.FIRST_RUN_ARG) == true) {
            return@LaunchedEffect
        }
        PendingChatLink.consume()
        if (AppContainer.settings.sessionRole != link.role) return@LaunchedEffect
        val sameChat = currentRoute == Routes.CONVERSATION &&
                currentEntry?.arguments?.getString(Routes.CHAT_ID_ARG) == link.chatId
        if (!sameChat) {
            navController.navigate(Routes.conversation(link.chatId, link.role))
        }
    }

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
            val autoRoute = remember {
                val settings = AppContainer.settings
                settings.sessionRole?.let { entryRouteFor(it) }
                    ?: Screen.Login.name.takeIf { settings.onboardingSeen }
            }
            BottomInsetsPane {
                WelcomeScreen(
                    onAutoContinue = autoRoute?.let { route ->
                        {
                            entry.ifTop(navController) {
                                navController.navigate(route) {
                                    popUpTo(Screen.Welcome.name) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onRegister = {
                        entry.ifResumed {
                            AppContainer.settings.markOnboardingSeen()
                            navController.navigate(Screen.Registration.name) { launchSingleTop = true }
                        }
                    },
                    onLogin = {
                        entry.ifResumed {
                            AppContainer.settings.markOnboardingSeen()
                            navController.navigate(Screen.Login.name) { launchSingleTop = true }
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
                            navController.navigate(Screen.Registration.name) { launchSingleTop = true }
                        }
                    },
                    onForgotPassword = {
                        entry.ifResumed {
                            navController.navigate(Screen.ForgotPassword.name) { launchSingleTop = true }
                        }
                    },
                    onSuccess = { role ->
                        AppContainer.settings.setSessionRole(role)
                        AppContainer.settings.setLastRole(role)
                        PushRegistrar.register(context)
                        entry.ifTop(navController) {
                            navController.navigate(entryRouteFor(role)) {
                                popUpTo(navController.graph.id) { inclusive = true }
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
                        entry.ifResumed { navController.openLogin() }
                    },
                    onSuccess = { role ->
                        AppContainer.settings.setLastRole(role)
                        entry.ifTop(navController) {
                            navController.navigate(Screen.EmailConfirm.name) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(Screen.EmailConfirm.name) { entry ->
            BottomInsetsPane {
                EmailConfirmScreen(
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    verify = { code -> AppContainer.auth.confirmCode(code) },
                    onSuccess = {
                        entry.ifTop(navController) { navController.openLogin() }
                    },
                    onResend = { }
                )
            }
        }

        composable(Screen.ForgotPassword.name) { entry ->
            BottomInsetsPane {
                ForgotPasswordScreen(
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onCodeSent = { target ->
                        entry.ifTop(navController) {
                            navController.navigate(Routes.resetCode(target)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.RESET_CODE,
            arguments = listOf(navArgument(Routes.TARGET_ARG) { type = NavType.StringType })
        ) { entry ->
            val target = entry.stringArg(Routes.TARGET_ARG).orEmpty()
            BottomInsetsPane {
                EmailConfirmScreen(
                    message = stringResource(R.string.text_19_6, target),
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onSuccess = {
                        entry.ifResumed {
                            navController.navigate(Screen.ResetPassword.name) { launchSingleTop = true }
                        }
                    },
                    onResend = { }
                )
            }
        }

        composable(Screen.ResetPassword.name) { entry ->
            BottomInsetsPane {
                ResetPasswordScreen(
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onDone = {
                        entry.ifTop(navController) {
                            if (!navController.popBackStack(Screen.Login.name, inclusive = false)) {
                                navController.popBackStack(Screen.ChangePassword.name, inclusive = true)
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.MAIN,
            arguments = listOf(
                navArgument(Routes.ROLE_ARG) {
                    type = NavType.StringType
                    defaultValue = UserRole.Owner.name
                }
            ),
            enterTransition = mainEnter
        ) { entry ->
            MainScreen(
                navController = navController,
                role = entry.roleArg()
            )
        }

        composable(
            route = Routes.SETTINGS,
            arguments = listOf(navArgument(Routes.ROLE_ARG) { type = NavType.StringType })
        ) { entry ->
            val role = entry.roleArg()
            BottomInsetsPane {
                SettingsScreen(
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onEditProfile = {
                        entry.ifResumed {
                            navController.navigate(Routes.editProfile(role)) { launchSingleTop = true }
                        }
                    },
                    onChangePassword = {
                        entry.ifResumed {
                            navController.navigate(Screen.ChangePassword.name) { launchSingleTop = true }
                        }
                    },
                    onOpenGuide = {
                        entry.ifResumed {
                            navController.navigate(Routes.appGuide(role)) { launchSingleTop = true }
                        }
                    },
                    onLogout = { entry.ifResumed { navController.logout(context) } }
                )
            }
        }

        composable(
            route = Routes.APP_GUIDE,
            arguments = listOf(
                navArgument(Routes.ROLE_ARG) { type = NavType.StringType },
                navArgument(Routes.FIRST_RUN_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = {
                if (initialState.destination.route == Screen.Login.name) mainEnter(this) else defaultEnter(this)
            }
        ) { entry ->
            val role = entry.roleArg()
            val firstRun = entry.arguments?.getBoolean(Routes.FIRST_RUN_ARG) ?: false
            BottomInsetsPane {
                AppGuideScreen(
                    role = role,
                    firstRun = firstRun,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onFinish = {
                        entry.ifResumed {
                            AppContainer.settings.markGuideSeen(role)
                            navController.navigate(Routes.main(role)) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.EDIT_PROFILE,
            arguments = listOf(navArgument(Routes.ROLE_ARG) { type = NavType.StringType })
        ) { entry ->
            BottomInsetsPane(includeIme = false) {
                EditProfileScreen(
                    role = entry.roleArg(),
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onSaved = { entry.ifTop(navController) { navController.popBackStack() } }
                )
            }
        }

        composable(Screen.ChangePassword.name) { entry ->
            BottomInsetsPane {
                ChangePasswordScreen(
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onForgotPassword = {
                        entry.ifResumed {
                            navController.navigate(Screen.ForgotPassword.name) { launchSingleTop = true }
                        }
                    },
                    onChanged = { entry.ifTop(navController) { navController.popBackStack() } }
                )
            }
        }

        composable(
            route = Routes.CONVERSATION,
            arguments = listOf(
                navArgument(Routes.ROLE_ARG) { type = NavType.StringType },
                navArgument(Routes.CHAT_ID_ARG) { type = NavType.StringType }
            )
        ) { entry ->
            val role = entry.roleArg()
            val chatId = entry.stringArg(Routes.CHAT_ID_ARG).orEmpty()
            BottomInsetsPane {
                key(chatId) {
                    ConversationScreen(
                        chatId = chatId,
                        role = role,
                        onBack = { entry.ifResumed { navController.popBackStack() } },
                        onOpenInfo = {
                            entry.ifResumed {
                                navController.navigate(Routes.chatInfo(chatId, role)) { launchSingleTop = true }
                            }
                        },
                        onOpenAttachment = { messageId ->
                            entry.ifResumed {
                                navController.navigate(Routes.attachment(chatId, messageId)) { launchSingleTop = true }
                            }
                        }
                    )
                }
            }
        }

        composable(
            route = Routes.CHAT_INFO,
            arguments = listOf(
                navArgument(Routes.ROLE_ARG) { type = NavType.StringType },
                navArgument(Routes.CHAT_ID_ARG) { type = NavType.StringType }
            )
        ) { entry ->
            val role = entry.roleArg()
            val chatId = entry.stringArg(Routes.CHAT_ID_ARG).orEmpty()
            BottomInsetsPane {
                ChatInfoScreen(
                    chatId = chatId,
                    role = role,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onOpenProfile = { volunteerId ->
                        entry.ifResumed {
                            navController.navigate(Routes.volunteerProfile(volunteerId)) { launchSingleTop = true }
                        }
                    },
                    onOpenRequest = { requestId ->
                        entry.ifResumed {
                            val route = when (role) {
                                UserRole.Owner -> Routes.responses(requestId)
                                UserRole.Volunteer -> Routes.requestDetails(requestId)
                            }
                            if (!navController.popBackStack(route, inclusive = false)) {
                                navController.navigate(route) { launchSingleTop = true }
                            }
                        }
                    },
                    onOpenAttachment = { messageId ->
                        entry.ifResumed {
                            navController.navigate(Routes.attachment(chatId, messageId)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.ATTACHMENT,
            arguments = listOf(
                navArgument(Routes.CHAT_ID_ARG) { type = NavType.StringType },
                navArgument(Routes.MESSAGE_ID_ARG) { type = NavType.StringType }
            ),
            enterTransition = sheetEnter,
            popExitTransition = sheetPopExit
        ) { entry ->
            BottomInsetsPane {
                AttachmentViewerScreen(
                    chatId = entry.stringArg(Routes.CHAT_ID_ARG).orEmpty(),
                    messageId = entry.stringArg(Routes.MESSAGE_ID_ARG).orEmpty(),
                    onBack = { entry.ifResumed { navController.popBackStack() } }
                )
            }
        }

        composable(
            route = Routes.RESPONSES,
            arguments = listOf(navArgument(Routes.REQUEST_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            val requestId = entry.stringArg(Routes.REQUEST_ID_ARG).orEmpty()
            BottomInsetsPane {
                ResponsesScreen(
                    requestId = requestId,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onVolunteerClick = { volunteerId ->
                        entry.ifResumed {
                            navController.navigate(Routes.volunteerProfile(volunteerId)) { launchSingleTop = true }
                        }
                    },
                    onEditRequest = { id ->
                        entry.ifResumed {
                            navController.navigate(Routes.createRequest(id)) { launchSingleTop = true }
                        }
                    },
                    onOpenChat = { chatId ->
                        entry.ifResumed {
                            navController.navigate(Routes.conversation(chatId, UserRole.Owner)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.VOLUNTEER_PROFILE,
            arguments = listOf(navArgument(Routes.VOLUNTEER_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            val volunteerId = entry.stringArg(Routes.VOLUNTEER_ID_ARG).orEmpty()
            BottomInsetsPane {
                VolunteerProfileScreen(
                    volunteerId = volunteerId,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onAllReviews = { id ->
                        entry.ifResumed {
                            navController.navigate(Routes.reviews(id)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.REVIEWS,
            arguments = listOf(navArgument(Routes.VOLUNTEER_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            BottomInsetsPane {
                ReviewsScreen(
                    volunteerId = entry.stringArg(Routes.VOLUNTEER_ID_ARG).orEmpty(),
                    onBack = { entry.ifResumed { navController.popBackStack() } }
                )
            }
        }

        composable(
            route = Routes.REQUEST_DETAILS,
            arguments = listOf(navArgument(Routes.REQUEST_ID_ARG) { type = NavType.StringType })
        ) { entry ->
            BottomInsetsPane {
                RequestDetailsScreen(
                    requestId = entry.stringArg(Routes.REQUEST_ID_ARG).orEmpty(),
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onOpenChat = { chatId ->
                        entry.ifResumed {
                            navController.navigate(Routes.conversation(chatId, UserRole.Volunteer)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.PET_PROFILE,
            arguments = listOf(
                navArgument(Routes.PET_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val petId = entry.stringArg(Routes.PET_ID_ARG)
            BottomInsetsPane(includeIme = false) {
                PetProfileScreen(
                    petId = petId,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onSave = { savedId ->
                        entry.ifTop(navController) {
                            if (petId == null && savedId != null) {
                                navController.previousBackStackEntry?.savedStateHandle?.set(ADDED_PET_KEY, savedId)
                            }
                            navController.popBackStack()
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.CREATE_REQUEST,
            arguments = listOf(
                navArgument(Routes.REQUEST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            exitTransition = {
                if (targetState.destination.route == Routes.MAP_PICKER) {
                    fadeOut(tween(NAV_DURATION))
                } else {
                    defaultExit(this)
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == Routes.MAP_PICKER) {
                    fadeIn(tween(NAV_DURATION))
                } else {
                    defaultPopEnter(this)
                }
            }
        ) { entry ->
            val pickedAddress by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_ADDRESS_KEY, null)
                .collectAsState()
            val addedPetId by entry.savedStateHandle
                .getStateFlow<String?>(ADDED_PET_KEY, null)
                .collectAsState()
            val pickedPoint by entry.savedStateHandle
                .getStateFlow<DoubleArray?>(PICKED_POINT_KEY, null)
                .collectAsState()

            BottomInsetsPane(includeIme = false) {
                CreateRequestScreen(
                    requestId = entry.stringArg(Routes.REQUEST_ID_ARG),
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onAddPet = {
                        entry.ifResumed {
                            navController.navigate(Routes.petProfile()) { launchSingleTop = true }
                        }
                    },
                    onCreate = { entry.ifTop(navController) { navController.popBackStack() } },
                    pickedAddress = pickedAddress,
                    pickedPoint = pickedPoint?.let { GeoPoint(it[0], it[1]) },
                    onPickedAddressUsed = {
                        entry.savedStateHandle[PICKED_ADDRESS_KEY] = null
                        entry.savedStateHandle[PICKED_POINT_KEY] = null
                    },
                    addedPetId = addedPetId,
                    onAddedPetUsed = {
                        entry.savedStateHandle[ADDED_PET_KEY] = null
                    },
                    onPickOnMap = { current ->
                        entry.ifResumed {
                            entry.savedStateHandle[MAP_START_KEY] = current?.let { doubleArrayOf(it.lat, it.lon) }
                            navController.navigate(Routes.mapPicker()) { launchSingleTop = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.MAP_PICKER,
            arguments = listOf(
                navArgument(Routes.FOR_VOLUNTEER_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = sheetEnter,
            popExitTransition = sheetPopExit
        ) { entry ->
            val forVolunteer = entry.arguments?.getBoolean(Routes.FOR_VOLUNTEER_ARG) ?: false
            val savedLocation = AppContainer.settings.volunteerLocation.collectAsState().value
            val ownerStart = remember(entry) {
                navController.previousBackStackEntry?.savedStateHandle
                    ?.get<DoubleArray>(MAP_START_KEY)
                    ?.let { GeoPoint(it[0], it[1]) }
            }
            BottomInsetsPane {
                MapPickerScreen(
                    forVolunteerLocation = forVolunteer,
                    startPoint = if (forVolunteer) savedLocation?.point else ownerStart,
                    onBack = { entry.ifResumed { navController.popBackStack() } },
                    onPicked = { address, point ->
                        entry.ifResumed {
                            if (forVolunteer) {
                                AppContainer.settings.setVolunteerLocation(SavedLocation(point, address))
                            } else {
                                navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                                    handle[PICKED_ADDRESS_KEY] = address
                                    handle[PICKED_POINT_KEY] = doubleArrayOf(point.lat, point.lon)
                                }
                            }
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}
