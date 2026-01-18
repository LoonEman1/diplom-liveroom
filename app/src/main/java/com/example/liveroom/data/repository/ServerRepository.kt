
package com.example.liveroom.data.repository
import android.content.Context
import android.net.Uri
import com.example.liveroom.data.remote.api.ServerApiService
import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.Server
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ServerRepository @Inject constructor(
    private val apiService: ServerApiService,
    private val context: Context
) {

    suspend fun getServers(userId: Int): Result<List<Server>> {
        return try {
            val serverList = apiService.getServers(userId)
            Result.success(serverList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createServer(
        name: String,
        imageUri: Uri?
    ): Result<Server> {
        return try {
            val requestData = CreateServerRequest(name = name)
            val jsonString = Gson().toJson(requestData)
            val dataRequestBody = jsonString.toRequestBody("application/json".toMediaType())

            val avatarPart = if (imageUri != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: byteArrayOf()

                val mimeType = when {
                    imageUri.toString().lowercase().endsWith(".png") -> "image/png"
                    imageUri.toString().lowercase().endsWith(".gif") -> "image/gif"
                    imageUri.toString().lowercase().endsWith(".webp") -> "image/webp"
                    imageUri.toString().lowercase().endsWith(".jpg") ||
                            imageUri.toString().lowercase().endsWith(".jpeg") -> "image/jpeg"
                    else -> context.contentResolver.getType(imageUri) ?: "image/jpeg"
                }

                val requestBody = bytes.toRequestBody(mimeType.toMediaType())

                MultipartBody.Part.createFormData(
                    "avatar",
                    imageUri.lastPathSegment ?: "avatar",
                    requestBody
                )
            } else {
                null
            }

            val response = if (avatarPart != null) {
                apiService.createServerWithAvatar(
                    data = dataRequestBody,
                    avatar = avatarPart
                )
            } else {
                apiService.createServerWithoutAvatar(
                    data = dataRequestBody
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /*suspend fun uploadServerAvatar(serverId: Int, serverName : String, imageUri: Uri): Result<Server> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File(context.cacheDir, "server_avatar_${System.currentTimeMillis()}.jpg")
            inputStream?.copyTo(tempFile.outputStream())

            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            apiService.uploadServerAvatar(serverId, multipartBody)

            val updatedServer = Server(
                id = serverId,
                name = serverName,
                avatarUrl = "http://194.226.49.64:8080/api/servers/$serverId/avatar"
            )

            tempFile.delete()
            Result.success(updatedServer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    } */
}
