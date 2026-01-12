package com.example.liveroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {
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