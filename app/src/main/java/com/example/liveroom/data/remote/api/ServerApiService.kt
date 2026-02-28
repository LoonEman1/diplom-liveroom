package com.example.liveroom.data.remote.api

import com.example.liveroom.data.remote.dto.Conversation
import com.example.liveroom.data.remote.dto.CreateConversationRequest
import com.example.liveroom.data.remote.dto.CreateServerRequest
import com.example.liveroom.data.remote.dto.EditMessageRequest
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.data.remote.dto.InviteRequest
import com.example.liveroom.data.remote.dto.InviteUserRequest
import com.example.liveroom.data.remote.dto.JoinByTokenRequest
import com.example.liveroom.data.remote.dto.Message
import com.example.liveroom.data.remote.dto.SendMessageRequest
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.remote.dto.ServerMember
import com.example.liveroom.data.remote.dto.UpdateServerRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
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
import retrofit2.http.Query

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

    @POST("api/me/server-invites/{inviteId}/accept")
    suspend fun acceptInvite (
        @Path("inviteId") inviteId : Int
    )

    @POST("api/me/server-invites/{inviteId}/decline")
    suspend fun declineInvite(
        @Path("inviteId") inviteId : Int
    ) : Response<Unit>

    @POST("api/servers/{serverId}/leave")
    suspend fun leaveFromServer(
        @Path("serverId") serverId : Int
    ) : Response<Unit>

    @GET("api/servers/{serverId}/conversations")
    suspend fun getServerConversations(@Path("serverId") serverId: Int): List<Conversation>

    @GET("api/servers/{serverId}/members")
    suspend fun getServerMembers(@Path("serverId") serverId: Int): List<ServerMember>

    @POST("api/servers/{serverId}/conversations")
    suspend fun createConversation(
        @Path("serverId") serverId: Int,
        @Body request: CreateConversationRequest
    ): Conversation


    @PUT("api/servers/{serverId}/conversations/{conversationId}")
    suspend fun updateConversation(
        @Path("serverId") serverId: Int,
        @Path("conversationId") conversationId: Long,
        @Body request: Map<String, String>
    ): Conversation

    @DELETE("api/servers/{serverId}/conversations/{conversationId}")
    suspend fun deleteConversation(
        @Path("serverId") serverId: Int,
        @Path("conversationId") conversationId: Long
    ): Response<Unit>

    @GET("api/servers/{serverId}/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("serverId") serverId: Long,
        @Path("conversationId") conversationId: Long,
        @Query("limit") limit: Int = 50,
        @Query("beforeMessageId") beforeMessageId: Long? = null
    ): List<Message>

    @POST("api/servers/{serverId}/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("serverId") serverId: Long,
        @Path("conversationId") conversationId: Long,
        @Body request: SendMessageRequest
    ): Message

    @PUT("/api/servers/{serverId}/conversations/{conversationId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("serverId") serverId: Long,
        @Path("conversationId") conversationId: Long,
        @Path("messageId") messageId: Long,
        @Body request: EditMessageRequest
    ): Message

    @DELETE("/api/servers/{serverId}/conversations/{conversationId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("serverId") serverId: Long,
        @Path("conversationId") conversationId: Long,
        @Path("messageId") messageId: Long
    ): Response<Unit>

    @POST("api/servers/{serverId}/conversations/{conversationId}/members")
    suspend fun inviteToConversation(
        @Path("serverId") serverId: Int,
        @Path("conversationId") conversationId: Long,
        @Body request: InviteRequest
    ): Response<Unit>
}

