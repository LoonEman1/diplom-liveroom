package com.example.liveroom.data.remote.dto

data class Server(
    val id : Int,
    val name : String,
    val avatarUrl : String? = null
)

data class CreateServerRequest(
    val name: String
)