package com.example.liveroom.data.remote.dto

data class ApiErrorResponse(
    val code: String,
    val message: String,
    val status: Int,
    val details: List<String> = emptyList()
)