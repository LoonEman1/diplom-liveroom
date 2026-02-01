package com.example.liveroom.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

fun Throwable.getServerErrorMessage(): String {
    if (this !is HttpException) return message ?: "Unknown error"

    return try {
        val response = response() ?: return "HTTP ${code()}"
        val errorBody = response.errorBody() ?: return "HTTP ${code()}"

        val bodyBytes = errorBody.bytes()
        val bodyString = String(bodyBytes, Charsets.UTF_8)

        Log.d("ErrorBody", "Raw: $bodyString")

        val json = Gson().fromJson(bodyString, JsonObject::class.java)
        val mainMessage = json.get("message")?.asString ?: "HTTP ${code()}"

        val details = json.getAsJsonArray("details")
        val detailMsg = if (!details.isJsonNull && details.size() > 0) {
            details[0].asJsonObject.get("message")?.asString ?: ""
        } else ""

        return if (detailMsg.isNotBlank()) "$mainMessage: $detailMsg"
        else mainMessage

    } catch (e: Exception) {
        Log.e("ErrorParse", "Failed to parse: ${e.message}", e)
        "HTTP ${code()}"
    }
}
