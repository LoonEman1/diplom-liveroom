package com.example.liveroom.ui.view.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.data.model.AuthFieldConfig
import com.example.liveroom.ui.navigation.Screen

@Composable
fun LoginView(navController: NavController) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val rememberMeState = remember { mutableStateOf(false) }


    AuthFormView(
        title = "Login",
        fields = listOf(
            AuthFieldConfig(
                label = "Login",
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = "Password",
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = "Login",
        onSubmit = {

        },
        showRememberMe = true,
        rememberMeValue = rememberMeState.value,
        onRememberMeChange = { newValue ->
            rememberMeState.value = newValue
        },
        navigationText = "Don't have an account? Sign up",
        onNavigationTextClick = {
            navController.navigate(Screen.RegistrationScreen.route)
        }
    )
}

@Preview
@Composable
fun PreviewLoginView() {
    val navController = rememberNavController()
    LoginView(navController)
}