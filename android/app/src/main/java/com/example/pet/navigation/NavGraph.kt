package com.example.pet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.name,
        modifier = modifier
    ) {
        composable(Screen.Welcome.name) {
            WelcomeScreen(onStart = { navController.navigate(Screen.Login.name) })
        }
        composable(Screen.Registration.name) {
            RegistrationScreen(
                onLoginClick = { navController.navigate(Screen.Login.name) },
                onSuccess = { navController.navigate(Screen.EmailConfirm.name) }
            )
        }
        composable(Screen.Login.name) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Screen.Registration.name) },
                onSuccess = { navController.navigate(Screen.PetProfile.name) }
            )
        }
        composable(Screen.PetProfile.name) {
            PetProfileScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.navigate(Screen.CreateRequest.name) } // Временно пока что
            )
        }
        composable(Screen.CreateRequest.name) { backStackEntry ->
            val pickedAddress by backStackEntry.savedStateHandle
                .getStateFlow<String?>(PICKED_ADDRESS_KEY, null)
                .collectAsState()
            CreateRequestScreen(
                onBack = { navController.popBackStack() },
                onSelectPet = { /* TODO: выбор питомца */ },
                onCreate = { navController.navigate(Screen.Main.name) },
                pickedAddress = pickedAddress,
                onPickedAddressUsed = { backStackEntry.savedStateHandle[PICKED_ADDRESS_KEY] = null },
                onPickOnMap = { navController.navigate(Screen.MapPicker.name) }
            )
        }
        composable(Screen.MapPicker.name) {
            MapPickerScreen(
                onBack = { navController.popBackStack() },
                onPicked = { address ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(PICKED_ADDRESS_KEY, address)
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.EmailConfirm.name) {
            EmailConfirmScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.navigate(Screen.Login.name) }, // Временно пока что
                onResend = { /* TODO: повторная отправка кода */ }
            )
        }
        composable(Screen.Main.name){
            MainScreen(navController)
        }
    }
}