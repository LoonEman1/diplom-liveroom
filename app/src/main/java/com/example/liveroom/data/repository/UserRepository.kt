package com.example.liveroom.data.repository

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import com.example.liveroom.data.remote.api.UserApiService
import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import com.example.liveroom.data.remote.dto.UpdateProfileRequest
import com.example.liveroom.data.remote.dto.UserInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: UserApiService,
    private val context: Context
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

    suspend fun updateAvatar(imageUri: Uri): Result<UserInfo> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?: byteArrayOf()

            val mimeType = getMimeType(imageUri)
            val requestBody = bytes.toRequestBody(mimeType.toMediaType())

            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                imageUri.lastPathSegment ?: "avatar.jpg",
                requestBody
            )

            val updatedUser = apiService.updateAvatar(avatarPart)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMimeType(imageUri: Uri): String {
        return when {
            imageUri.toString().lowercase().endsWith(".png") -> "image/png"
            imageUri.toString().lowercase().endsWith(".gif") -> "image/gif"
            imageUri.toString().lowercase().endsWith(".webp") -> "image/webp"
            imageUri.toString().lowercase().endsWith(".jpg") ||
                    imageUri.toString().lowercase().endsWith(".jpeg") -> "image/jpeg"
            else -> context.contentResolver.getType(imageUri) ?: "image/jpeg"
        }
    }
}