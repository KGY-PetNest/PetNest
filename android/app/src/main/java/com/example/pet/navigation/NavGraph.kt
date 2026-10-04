package com.example.pet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.name,
        modifier = modifier
    ) {
        composable(Screen.Welcome.name) {
            BottomInsetsPane {
                WelcomeScreen(
                    onStart = {
                        navController.navigate(Screen.Login.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.Login.name) {
            BottomInsetsPane {
                LoginScreen(
                    onRegisterClick = {
                        navController.navigate(Screen.Registration.name) {
                            launchSingleTop = true
                        }
                    },
                    onSuccess = {
                        navController.navigate(Screen.Main.name) {
                            popUpTo(Screen.Welcome.name) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.Registration.name) {
            BottomInsetsPane {
                RegistrationScreen(
                    onLoginClick = {
                        navController.popBackStack(Screen.Login.name, inclusive = false)
                    },
                    onSuccess = {
                        navController.navigate(Screen.EmailConfirm.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.EmailConfirm.name) {
            BottomInsetsPane {
                EmailConfirmScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack(Screen.Login.name, inclusive = false)
                    },
                    onResend = {
                        /* TODO: повторная отправка кода */
                    }
                )
            }
        }

        // Main без BottomInsetsPane: нижняя панель сама прижата к низу
        composable(Screen.Main.name) {
            MainScreen(navController)
        }

        composable(Screen.PetProfile.name) {
            BottomInsetsPane {
                PetProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSave = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.CreateRequest.name) { backStackEntry ->
            val pickedAddress by backStackEntry.savedStateHandle
                .getStateFlow<String?>(PICKED_ADDRESS_KEY, null)
                .collectAsState()

            BottomInsetsPane {
                CreateRequestScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSelectPet = {
                        /* TODO: выбор питомца */
                    },
                    onCreate = {
                        navController.popBackStack()
                    },
                    pickedAddress = pickedAddress,
                    onPickedAddressUsed = {
                        backStackEntry.savedStateHandle[PICKED_ADDRESS_KEY] = null
                    },
                    onPickOnMap = {
                        navController.navigate(Screen.MapPicker.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.MapPicker.name) {
            BottomInsetsPane {
                MapPickerScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onPicked = { address ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(PICKED_ADDRESS_KEY, address)

                        navController.popBackStack()
                    }
                )
            }
        }
    }
}