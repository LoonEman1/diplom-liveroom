package com.example.liveroom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.liveroom.ui.view.auth.LoginView
import com.example.liveroom.ui.view.auth.RegistrationView
import com.example.liveroom.ui.view.main.MainView
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun NavigationGraph(
    navController : NavHostController,
    startDestination : String
) {

    val userViewModel: UserViewModel = hiltViewModel<UserViewModel>()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(
            route = Screen.LoginScreen.route
        ) {
            LoginView(navController, userViewModel)
        }

        composable (
            route = Screen.RegistrationScreen.route
        )
        {
            RegistrationView(navController, userViewModel)
        }

        composable(
            route = Screen.MainScreen.route
        ) {
            MainView(navController, userViewModel)
        }
    }
}