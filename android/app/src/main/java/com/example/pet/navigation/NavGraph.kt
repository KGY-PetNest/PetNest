package com.example.pet.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet.ui.login.LoginScreen
import com.example.pet.ui.registration.RegistrationScreen
import com.example.pet.ui.welcome.WelcomeScreen

@Composable
fun NavGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(navController = navController, startDestination = Screen.Welcome.name) {
        composable(Screen.Welcome.name) {
            WelcomeScreen(onStart = { navController.navigate(Screen.Login.name) })
        }
        composable(Screen.Registration.name) {
            RegistrationScreen(
                onBack = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Screen.Login.name) },
                onSuccess = {  }
            )
        }
        composable(Screen.Login.name) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(Screen.Registration.name) },
                onSuccess = {  }
            )
        }
    }
}