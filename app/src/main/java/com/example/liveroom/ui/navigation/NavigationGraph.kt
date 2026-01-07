package com.example.liveroom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.liveroom.ui.view.auth.LoginView
import com.example.liveroom.ui.view.auth.RegistrationView
import com.example.liveroom.ui.view.main.MainView

@Composable
fun NavigationGraph(
    navController : NavHostController,
    startDestination : String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(
            route = Screen.LoginScreen.route
        ) {
            LoginView(navController)
        }

        composable (
            route = Screen.RegistrationScreen.route
        )
        {
            RegistrationView(navController)
        }

        composable(
            route = Screen.MainScreen.route
        ) {
            MainView(navController)
        }
    }
}