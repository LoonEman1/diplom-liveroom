package com.example.liveroom.ui.view.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.R
import com.example.liveroom.data.model.AuthFieldConfig
import com.example.liveroom.ui.navigation.Screen

@Composable
fun LoginView(navController: NavController) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val rememberMeState = remember { mutableStateOf(false) }


    AuthFormView(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = loginState.value,
                onValueChange = { loginState.value = it },
                fieldType = "login"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                fieldType = "password"
            )
        ),
        submitButtonText = stringResource(R.string.sign_in),
        onSubmit = {
            navController.navigate(Screen.MainScreen.route)
        },
        showRememberMe = true,
        rememberMeValue = rememberMeState.value,
        onRememberMeChange = { newValue ->
            rememberMeState.value = newValue
        },
        navigationText = stringResource(R.string.login_nav_text),
        onNavigationTextClick = {
            navController.navigate(Screen.RegistrationScreen.route)
        },
        signText = stringResource(R.string.sign_up),
        icon = {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
            )
        }
    )
}

@Preview
@Composable
fun PreviewLoginView() {
    val navController = rememberNavController()
    LoginView(navController)
}