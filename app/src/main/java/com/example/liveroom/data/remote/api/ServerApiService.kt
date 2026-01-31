package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.data.remote.dto.InviteUserRequest
import com.example.liveroom.data.remote.dto.JoinByTokenRequest
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.remote.dto.UpdateServerRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ServerApiService {
    @GET("api/users/{userId}/servers")
    suspend fun getServers(
        @Path("userId") userId: Int,
    ): List<Server>

    @Multipart
    @POST("api/servers")
    suspend fun createServerWithAvatar(
        @Part("data") data: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Server

    @POST("api/servers")
    suspend fun createServerWithoutAvatar(
        @Body data: RequestBody
    ): Server


    @Multipart
    @PUT("api/servers/{serverId}")
    suspend fun updateServerNameAndAvatar(
        @Path("serverId") serverId: Int,
        @Part("data") data: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ) : Server

    @PATCH("api/servers/{serverId}")
    suspend fun updateServerName(
        @Path("serverId") serverId: Int,
        @Body request: UpdateServerRequest
    ) : Server

    @Multipart
    @PUT("api/servers/{serverId}/avatar")
    suspend fun uploadServerAvatar(
        @Path("serverId") serverId : Int,
        @Part file: MultipartBody.Part,
    ) : Server

    @GET("api/servers/{serverId}/avatar")
    suspend fun getServerAvatar(
        @Path("serverId") serverId: Int
    ): ResponseBody

    @DELETE("api/servers/{serverId}")
    suspend fun deleteServer(
        @Path("serverId") serverId : Int
    )

    @POST("api/servers/{serverId}/invites/token")
    suspend fun createToken(
        @Path("serverId") serverId : Int
    ) : Invite.TokenInvite

    @POST("api/servers/{serverId}/invites/user")
    suspend fun inviteUser(
        @Path("serverId") serverId : Int,
        @Body body : InviteUserRequest
    ) : Invite.UserInvite

    @POST("api/me/server-invites/join")
    suspend fun joinByToken(
        @Body body : JoinByTokenRequest
    ) : Server

    @GET("api/me/server-invites")
    suspend fun getInvites() : List<Invite.UserInvite>
}