package com.example.liveroom.data.model

data class AuthFieldConfig(
    val label : String,
    val value : String,
    val onValueChange : (String) -> Unit,
    val fieldType : String,
)
