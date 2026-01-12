package com.example.liveroom.data.remote.dto

data class RegisterRequest(
    val username : String,
    val email : String,
    val password : String,
    val confirmPassword : String
)

data class LoginRequest(
    val username: String,
    val password: String
)