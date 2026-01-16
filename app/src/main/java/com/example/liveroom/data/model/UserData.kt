package com.example.liveroom.data.model

data class UserData(
    val userId : Int,
    val nickname : String,
    val accessToken : String,
    val refreshToken : String,
    val rememberMe : Boolean
)
