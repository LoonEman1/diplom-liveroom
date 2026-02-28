package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import com.example.liveroom.data.remote.dto.UpdateProfileRequest
import com.example.liveroom.data.remote.dto.UserInfo
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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

    @Multipart
    @PUT("api/me/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): UserInfo

}