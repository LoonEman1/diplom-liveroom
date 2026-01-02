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
fun RegistrationView(navController: NavController) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val rememberMeState = remember { mutableStateOf(false) }
    val emailState = remember { mutableStateOf("") }


    AuthFormView(
        title = "Registration",
        fields = listOf(
            AuthFieldConfig(
                label = "Login",
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = "Email",
                value = emailState.value,
                onValueChange = { emailState.value = it },
                fieldType = "email"
            ),
            AuthFieldConfig(
                label = "Password",
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = "Register",
        onSubmit = {
        },
        showRememberMe = true,
        rememberMeValue = rememberMeState.value,
        onRememberMeChange = { newValue ->
            rememberMeState.value = newValue
        },
        navigationText = "Already have an account? Sign in",
        onNavigationTextClick = {
            navController.navigate(Screen.LoginScreen.route)
        }
    )
}

@Preview
@Composable
fun PreviewRegistrationView() {
    val navController = rememberNavController()
    RegistrationView(navController)
}