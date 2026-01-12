package com.example.liveroom.data.repository

import android.util.Log
import com.example.liveroom.data.remote.api.AuthService
import com.example.liveroom.data.remote.dto.AuthResponse
import com.example.liveroom.data.remote.dto.LoginRequest
import com.example.liveroom.data.remote.dto.RegisterRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(private val authService: AuthService) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun register(
        username : String,
        email : String,
        password : String,
        confirmPassword : String
    ) : Result<AuthResponse> = try {
        Log.d(TAG, "Starting registration for user: $username")

        val response = authService.register(
            RegisterRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword
            )
        )

        Log.d(TAG, "Registration successful for user: $username")
        Result.success(response)
    } catch (e : Exception) {
        Log.e(TAG, "Registration failed for user: $username", e)
        Result.failure(e)
    }

    suspend fun login(
        username: String,
        password: String
    ): Result<AuthResponse> = try {
        Log.d(TAG, "Starting login for: $username")

        val response = authService.login(
            LoginRequest(
                username = username,
                password = password
            )
        )

        Log.d(TAG, "Login successful for: $username")
        Result.success(response)
    } catch (e : Exception) {
        Log.e(TAG, "Login failed for: $username", e)
        Result.failure(e)
    }

    suspend fun logout() {
        Log.d(TAG, "User logged out")
    }
}
