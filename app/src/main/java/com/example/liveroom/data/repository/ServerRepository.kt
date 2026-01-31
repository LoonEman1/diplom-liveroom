
package com.example.liveroom.data.repository
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.liveroom.data.remote.api.ServerApiService
import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.data.remote.dto.InviteUserRequest
import com.example.liveroom.data.remote.dto.JoinByTokenRequest
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.remote.dto.UpdateServerRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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

                val mimeType = getMimeType(imageUri)

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

    suspend fun deleteServer(serverId: Int) : Result<Unit> {
        return try {
            val deleteServer = apiService.deleteServer(serverId)
            Result.success(deleteServer)
        } catch (e : Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateServer(
        serverId: Int,
        name: String?,
        imageUri: String?
    ): Result<Server> {
        if (name == null && imageUri == null) {
            return Result.failure(Exception("No changes to update"))
        }
        return try {
            val response : Server = when {
                name != null && imageUri != null -> {
                    val requestData = UpdateServerRequest(name = name)
                    val jsonString = Gson().toJson(requestData)
                    val dataRequestBody = jsonString.toRequestBody("application/json".toMediaType())

                    val inputStream = context.contentResolver.openInputStream(imageUri.toUri())
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()

                    val mimeType = getMimeType(imageUri.toUri())
                    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                    val avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        imageUri.toUri().lastPathSegment ?: "avatar",
                        requestBody
                    )

                    apiService.updateServerNameAndAvatar(
                        serverId = serverId,
                        data = dataRequestBody,
                        avatar = avatarPart
                    )
                }
                name != null && imageUri == null -> {
                    apiService.updateServerName(
                        serverId = serverId,
                        UpdateServerRequest(name)
                    )
                }
                imageUri != null -> {
                    val inputStream = context.contentResolver.openInputStream(imageUri.toUri())
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()

                    val mimeType = getMimeType(imageUri.toUri())
                    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                    val avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        imageUri.toUri().lastPathSegment ?: "avatar",
                        requestBody
                    )

                    apiService.uploadServerAvatar(
                        serverId = serverId,
                        file = avatarPart
                    )
                }
                else -> throw Exception("No changes to update")
            }

            Result.success(response)
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


    suspend fun createServerToken(serverId : Int) : Result<Invite.TokenInvite> {
        return try {
            val response : Invite.TokenInvite = apiService.createToken(serverId)
            Log.d("createServerToken", response.token)
            Result.success(response)
        } catch(e : Exception) {
            Log.e("createServerToken", e.message ?: "unknown error")
            Result.failure(e)
        }
    }

    suspend fun inviteUser(serverId: Int, username : String) : Result<Invite.UserInvite> {
        return try {
            val response : Invite.UserInvite = apiService.inviteUser(serverId, InviteUserRequest(username))
            Log.d("inviteUserToServer", response.inviteId.toString())
            Result.success(response)
        } catch(e : Exception) {
            Log.e("inviteUserToServer", e.message ?: "unknown error")
            Result.failure(e)
        }
    }

    suspend fun joinByToken(token : String) : Result<Server> {
        return try {
            val response : Server = apiService.joinByToken(JoinByTokenRequest(token))
            Log.d("joinByToken", response.name)
            Result.success(response)
        } catch (e : Exception) {
            Log.e("joinByToken", e.message ?: "unknown error")
            Result.failure(e)
        }
    }

    suspend fun getInvites() : Result<List<Invite.UserInvite>> {
        return try {
            val response: List<Invite.UserInvite> = apiService.getInvites()
            Log.d("getInvites", response.toString())
            Result.success(response)
        } catch(e : Exception) {
            Log.e("getInvites", e.message ?: "unknown  error")
            Result.failure(e)
        }
    }
}
