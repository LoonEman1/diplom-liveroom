package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.AuthResponse
import com.example.liveroom.data.remote.dto.LoginRequest
import com.example.liveroom.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/register")
    suspend fun register(@Body request : RegisterRequest) : AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest) : AuthResponse
}