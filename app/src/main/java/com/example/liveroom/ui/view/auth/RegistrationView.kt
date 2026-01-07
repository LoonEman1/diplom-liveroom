package com.example.liveroom.ui.view.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.R
import com.example.liveroom.data.model.AuthFieldConfig
import com.example.liveroom.ui.navigation.Screen

@Composable
fun RegistrationView(navController: NavController) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val rememberMeState = remember { mutableStateOf(false) }
    val emailState = remember { mutableStateOf("") }


    AuthFormView(
        title = stringResource(R.string.registration_title),
        subtitle = stringResource(R.string.registration_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.email),
                value = emailState.value,
                onValueChange = { emailState.value = it },
                fieldType = "email"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = stringResource(R.string.sign_up),
        onSubmit = {
            navController.navigate(Screen.MainScreen.route)
        },
        showRememberMe = true,
        rememberMeValue = rememberMeState.value,
        onRememberMeChange = { newValue ->
            rememberMeState.value = newValue
        },
        navigationText = stringResource(R.string.reg_nav_text),
        onNavigationTextClick = {
            navController.navigate(Screen.LoginScreen.route)
        },
        signText = stringResource(R.string.sign_in)
    )
}

@Preview
@Composable
fun PreviewRegistrationView() {
    val navController = rememberNavController()
    RegistrationView(navController)
}