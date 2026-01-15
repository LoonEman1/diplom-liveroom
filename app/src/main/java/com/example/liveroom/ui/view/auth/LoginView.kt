package com.example.liveroom.ui.view.auth

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.R
import com.example.liveroom.data.model.AuthFieldConfig
import com.example.liveroom.ui.navigation.Screen
import com.example.liveroom.ui.viewmodel.AuthState
import com.example.liveroom.ui.viewmodel.AuthViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun LoginView(navController: NavController, userViewModel: UserViewModel) {


    val viewModel: AuthViewModel = hiltViewModel<AuthViewModel>()

    val usernameState by viewModel.usernameState.collectAsState()
    val passwordState by viewModel.passwordState.collectAsState()

    val rememberMeState by viewModel.rememberMe.collectAsState()

    val loginState by viewModel.loginState.collectAsState()

    val context = LocalContext.current
    val errorMessage = stringResource(R.string.auth_error)

    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Success -> {
                val response = (loginState as AuthState.Success).response

                userViewModel.setUserData(
                    userId = response.userId,
                    username = response.username,
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )

                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    AuthFormView(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = usernameState,
                onValueChange = { viewModel.setUsernameValue(it) },
                fieldType = "username"
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState,
                onValueChange = { viewModel.setPasswordValue(it) },
                fieldType = "password"
            )
        ),
        submitButtonText = stringResource(R.string.sign_in),
        onSubmit = {
            viewModel.login(usernameState, passwordState)
        },
        showRememberMe = true,
        rememberMeValue = rememberMeState,
        onRememberMeChange = { newValue ->
            viewModel.setRememberMeValue(newValue)
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
    val userViewModel = hiltViewModel<UserViewModel>()
    LoginView(navController, userViewModel)
}