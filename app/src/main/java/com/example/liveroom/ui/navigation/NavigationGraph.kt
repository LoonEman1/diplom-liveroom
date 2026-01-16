package com.example.liveroom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.ui.view.auth.LoginView
import com.example.liveroom.ui.view.auth.RegistrationView
import com.example.liveroom.ui.view.main.MainView
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel
import kotlinx.coroutines.runBlocking

@Composable
fun NavigationGraph(
    navController : NavHostController,
    tokenManager: TokenManager
) {

    val userViewModel: UserViewModel = hiltViewModel<UserViewModel>()
    val serverViewModel : ServerViewModel = hiltViewModel<ServerViewModel>()

    val startDestination = remember {
        runBlocking {
            val userData = tokenManager.getUserData()

            if (userData != null) {
                userViewModel.setUserData(
                    userId = userData.userId,
                    username = userData.nickname,
                    accessToken = userData.accessToken,
                    refreshToken = userData.refreshToken
                )
                Screen.MainScreen.route
            } else {
                Screen.LoginScreen.route
            }
        }
    }


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
            MainView(navController, userViewModel, serverViewModel)
        }
    }
}