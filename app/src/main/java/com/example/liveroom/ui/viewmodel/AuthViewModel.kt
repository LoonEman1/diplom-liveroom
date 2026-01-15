package com.example.liveroom.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveroom.data.remote.dto.AuthResponse
import com.example.liveroom.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
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

    fun setUsernameValue(usernameValue: String) {
        _usernameState.value = usernameValue
    }

    fun setEmailValue(emailValue : String) {
        _emailState.value = emailValue
    }

    fun setPasswordValue(passwordValue : String) {
        _passwordState.value = passwordValue
    }

    fun setConfirmPasswordValue(confirmPasswordValue : String) {
        _confirmPasswordState.value = confirmPasswordValue
    }

    fun setRememberMeValue(state : Boolean) {
        _rememberMe.value = state
    }

    fun register(
        username : String,
        email : String,
        password : String,
        confirmPassword : String
    ) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            val result = repository.register(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword
            )
            _registerState.value = if(result.isSuccess) {
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown Error"
                )
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = repository.login(
                username = username,
                password = password
            )
            _loginState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrNull()!!)
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