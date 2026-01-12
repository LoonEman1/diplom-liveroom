package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.Server
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ServerApiService {
    @GET("api/users/{userId}/servers")
    suspend fun getServers(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): List<Server>

    @POST("api/servers")
    suspend fun createServer(
        @Body request: CreateServerRequest,
        @Header("Authorization") token: String
    ) : Server

    @Multipart
    @PUT("api/servers/{serverId}/avatar")
    suspend fun uploadServerAvatar(
        @Path("serverId") serverId : Int,
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ) : Unit

    @GET("api/servers/{serverId}/avatar")
    suspend fun getServerAvatar(
        @Path("serverId") serverId: Int
    ): ResponseBody
}