package com.example.liveroom.data.remote.dto

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class RefreshTokenRequest(
    val refreshToken : String
)

fun RefreshTokenRequest.toRequestBody(): RequestBody {
    val json = Gson().toJson(this)
    return json.toRequestBody("application/json".toMediaType())
}
