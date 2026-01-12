package com.example.liveroom.data.remote.dto

data class AuthResponse(
    val userId : Int,
    val username : String,
    val accessToken : String,
    val refreshToken : String
)
