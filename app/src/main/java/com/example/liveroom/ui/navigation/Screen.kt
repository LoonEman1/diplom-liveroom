package com.example.liveroom.ui.navigation

sealed class Screen(val route : String) {

    object LoginScreen : Screen(route = "login_screen")

    object RegistrationScreen : Screen(route = "reg_screen")
}