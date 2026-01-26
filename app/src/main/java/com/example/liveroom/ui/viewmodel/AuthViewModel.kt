package com.example.liveroom.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.remote.dto.AuthResponse
import com.example.liveroom.data.repository.AuthRepository
import com.example.liveroom.util.AuthValidators
import com.example.liveroom.util.ValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository, private val tokenManager: TokenManager) : ViewModel() {
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState =_registerState.asStateFlow()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _usernameState = MutableStateFlow("")
    val usernameState = _usernameState.asStateFlow()

    private val _emailState = MutableStateFlow("")
    val emailState = _emailState.asStateFlow()

    private val _passwordState = MutableStateFlow("")
    val passwordState = _passwordState.asStateFlow()

    private val _confirmPasswordState = MutableStateFlow("")
    val confirmPasswordState = _confirmPasswordState.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe = _rememberMe.asStateFlow()

    private val _usernameError = MutableStateFlow<ValidationError?>(null)
    val usernameError = _usernameError.asStateFlow()

    private val _emailError = MutableStateFlow<ValidationError?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<ValidationError?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<ValidationError?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    private val _showToast = MutableStateFlow<String?>(null)
    val showToast = _showToast.asStateFlow()

    fun setUsernameValue(usernameValue: String) {
        _usernameState.value = usernameValue
        _usernameError.value = AuthValidators.validateUsername(usernameValue)
    }

    fun setEmailValue(emailValue : String) {
        _emailState.value = emailValue
        _emailError.value = AuthValidators.validateEmail(emailValue)
    }

    fun setPasswordValue(passwordValue : String) {
        _passwordState.value = passwordValue
        _passwordError.value = AuthValidators.validatePassword(passwordValue)
        if (_confirmPasswordState.value.isNotEmpty()) {
            _confirmPasswordError.value = AuthValidators.validateConfirmPassword(
                passwordValue,
                _confirmPasswordState.value
            )
        }
    }

    fun setConfirmPasswordValue(confirmPasswordValue : String) {
        _confirmPasswordState.value = confirmPasswordValue
        _confirmPasswordError.value = AuthValidators.validateConfirmPassword(
            _passwordState.value,
            confirmPasswordValue
        )
    }

    fun setRememberMeValue(state : Boolean) {
        _rememberMe.value = state
    }

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val errors = AuthValidators.validateRegistration(
            username, email, password, confirmPassword
        )
        if (errors.isNotEmpty()) {
            errors.forEach { error ->
                when (error) {
                    is ValidationError.UsernameRequired,
                    is ValidationError.UsernameTooShort,
                    is ValidationError.UsernameTooLong,
                    is ValidationError.UsernameInvalidFormat -> {
                        _usernameError.value = error
                    }
                    is ValidationError.EmailRequired,
                    is ValidationError.EmailTooLong,
                    is ValidationError.EmailInvalid -> {
                        _emailError.value = error
                    }
                    is ValidationError.PasswordRequired,
                    is ValidationError.PasswordTooShort,
                    is ValidationError.PasswordTooLong,
                    is ValidationError.PasswordInvalid -> {
                        _passwordError.value = error
                    }
                    is ValidationError.ConfirmPasswordRequired,
                    is ValidationError.PasswordsDoNotMatch -> {
                        _confirmPasswordError.value = error
                    }
                    else -> {}
                }
            }
            return
        }

        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            val result = repository.register(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword
            )
            _registerState.value = if (result.isSuccess) {
                val authResponse = result.getOrNull()!!
                if (_rememberMe.value) {
                    tokenManager.saveUserData(
                        userId = authResponse.userId,
                        nickname = authResponse.username,
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        rememberMe = true
                    )
                }
                AuthState.Success(authResponse)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown Error"
                _showToast.value = errorMsg
                _registerState.value = AuthState.Error(errorMsg)
                AuthState.Error(errorMsg)
            }
        }
    }

    fun clearToast() {
        _showToast.value = null
    }

    fun login(username: String, password: String) {

        val errors = AuthValidators.validateLogin(username, password)
        if (errors.isNotEmpty()) {
            errors.forEach { error ->
                when (error) {
                    is ValidationError.UsernameRequired,
                    is ValidationError.UsernameTooShort,
                    is ValidationError.UsernameTooLong,
                    is ValidationError.UsernameInvalidFormat -> {
                        _usernameError.value = error
                    }
                    is ValidationError.PasswordRequired,
                    is ValidationError.PasswordTooShort,
                    is ValidationError.PasswordTooLong,
                    is ValidationError.PasswordInvalid -> {
                        _passwordError.value = error
                    }
                    else -> {}
                }
            }
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = repository.login(
                username = username,
                password = password
            )
            _loginState.value = if (result.isSuccess) {
                val authResponse = result.getOrNull()!!
                if (_rememberMe.value) {
                    tokenManager.saveUserData(
                        userId = authResponse.userId,
                        nickname = authResponse.username,
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        rememberMe = true
                    )
                }

                AuthState.Success(authResponse)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}