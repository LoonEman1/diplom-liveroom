package com.example.liveroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val tokenManager: TokenManager) : ViewModel() {
    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    private val _username = MutableStateFlow<String>("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _accessToken = MutableStateFlow<String>("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String>("")
    val refreshToken: StateFlow<String> = _refreshToken.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        viewModelScope.launch {
            _accessToken.collect { token ->
                if (token.isNotEmpty()) {
                    tokenManager.saveAccessToken(token)
                }
                else if(token.isEmpty()) {
                    _accessToken.value = tokenManager.getAccessToken() ?: "null"
                }
            }
        }

    }

    fun setUserData(
        userId: Int? = null,
        username: String? = null,
        accessToken: String? = null,
        refreshToken: String? = null
    ) {
        userId?.let { _userId.value = it }
        username?.let { _username.value = it }
        accessToken?.let { _accessToken.value = it }
        refreshToken?.let { _refreshToken.value = it }

        if (userId != null && accessToken != null) {
            _isAuthenticated.value = true
        }
    }

    fun clearUserData() {
        _userId.value = null
        _username.value = ""
        _accessToken.value = ""
        _refreshToken.value = ""
        _isAuthenticated.value = false
    }

}