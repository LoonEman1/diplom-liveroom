package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import com.example.liveroom.data.remote.dto.UpdateProfileRequest
import com.example.liveroom.data.remote.dto.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {

    @GET("/api/me")
    suspend fun getUserInfo() : UserInfo

    @POST("api/auth/logout")
    suspend fun logout(
        @Body body : RefreshTokenRequest
    )

    @PATCH("api/me/profile")
    suspend fun editProfile(
        @Body body : UpdateProfileRequest
    ) : UserInfo
}