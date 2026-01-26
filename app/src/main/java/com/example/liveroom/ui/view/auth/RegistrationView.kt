package com.example.liveroom.ui.view.auth

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.liveroom.ui.theme.LiveRoomTheme
import com.example.liveroom.ui.viewmodel.AuthState
import com.example.liveroom.ui.viewmodel.AuthViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun RegistrationView(navController: NavController, userViewModel: UserViewModel) {

    val viewModel: AuthViewModel = hiltViewModel<AuthViewModel>()

    val usernameState by viewModel.usernameState.collectAsState()
    val passwordState by viewModel.passwordState.collectAsState()
    val confirmPasswordState by viewModel.confirmPasswordState.collectAsState()
    val rememberMeState by viewModel.rememberMe.collectAsState()
    val emailState by viewModel.emailState.collectAsState()

    val registerState by viewModel.registerState.collectAsState()

    val usernameError by viewModel.usernameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val showToast by viewModel.showToast.collectAsState()


    val context = LocalContext.current
    val errorMessage = stringResource(R.string.auth_error)

    LaunchedEffect(showToast) {
        showToast?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is AuthState.Success -> {

                val response = (registerState as AuthState.Success).response

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
        title = stringResource(R.string.registration_title),
        subtitle = stringResource(R.string.registration_subtitle),
        fields = listOf(
            AuthFieldConfig(
                label = stringResource(R.string.nickname),
                value = usernameState,
                onValueChange = { viewModel.setUsernameValue(it) },
                fieldType = "username",
                isError = usernameError
            ),
            AuthFieldConfig(
                label = stringResource(R.string.email),
                value = emailState,
                onValueChange = { viewModel.setEmailValue(it) },
                fieldType = "email",
                isError = emailError
            ),
            AuthFieldConfig(
                label = stringResource(R.string.password),
                value = passwordState,
                onValueChange = { viewModel.setPasswordValue(it) },
                fieldType = "password",
                isError = passwordError
            ),
            AuthFieldConfig(
                label = stringResource(R.string.confirm_password),
                value = confirmPasswordState,
                onValueChange = { viewModel.setConfirmPasswordValue(it) },
                fieldType = "password",
                isError = confirmPasswordError
            )
        ),
        submitButtonText = stringResource(R.string.sign_up),
        onSubmit = {
            viewModel.register(
                usernameState,
                emailState,
                passwordState,
                confirmPasswordState
            )
        },
        showRememberMe = true,
        rememberMeValue = rememberMeState,
        onRememberMeChange = { newValue ->
            viewModel.setRememberMeValue(newValue)
        },
        navigationText = stringResource(R.string.reg_nav_text),
        onNavigationTextClick = {
            navController.navigate(Screen.LoginScreen.route)
        },
        signText = stringResource(R.string.sign_in),
        icon = {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
            )
        }
    )
}

/*
@Preview
@Composable
fun PreviewRegistrationView() {
    LiveRoomTheme {
        val navController = rememberNavController()
        val userViewModel = hiltViewModel<UserViewModel>()
        RegistrationView(navController, userViewModel)
    }
}
 */