package com.example.liveroom.data.model

import com.example.liveroom.util.ValidationError

data class AuthFieldConfig(
    val label : String,
    val value : String,
    val onValueChange : (String) -> Unit,
    val fieldType : String,
    val isError : ValidationError? = null
)
