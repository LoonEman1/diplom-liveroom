package com.example.liveroom.data.repository

import com.example.liveroom.data.remote.api.UserApiService
import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import com.example.liveroom.data.remote.dto.UpdateProfileRequest
import com.example.liveroom.data.remote.dto.UserInfo
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: UserApiService
){
    suspend fun getUserInfo() : Result<UserInfo> {
        return try {
            val userInfo = apiService.getUserInfo()
            Result.success(userInfo)
        } catch (e : Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            apiService.logout(RefreshTokenRequest(refreshToken))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editProfile(request: UpdateProfileRequest): Result<UserInfo> {
        return try {
            val updatedUser = apiService.editProfile(request)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}