
package com.example.liveroom.data.repository
import android.content.Context
import android.net.Uri
import com.example.liveroom.data.remote.api.ServerApiService
import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.Server
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

    suspend fun createServer(name: String): Result<Server> {
        return try {
            val request = CreateServerRequest(name = name)
            val newServer = apiService.createServer(request)
            Result.success(newServer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun uploadServerAvatar(serverId: Int, serverName : String, imageUri: Uri): Result<Server> {
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
    }
}
