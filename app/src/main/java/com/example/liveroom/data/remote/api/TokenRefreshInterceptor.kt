package com.example.liveroom.di

import android.util.Log
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.remote.api.AuthService
import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private var authService: AuthService? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        if (url.contains("api/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        val requestWithToken = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        var response = chain.proceed(requestWithToken)

        if (response.code == 401) {
            Log.d("TokenRefreshInterceptor", "Got 401, attempting token refresh")

            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                try {
                    if (authService == null) {
                        val cleanClient = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build()

                        val tempRetrofit = Retrofit.Builder()
                            .baseUrl("https://nighthunting23.ru/")
                            .client(cleanClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        authService = tempRetrofit.create(AuthService::class.java)
                    }

                    val result = runBlocking {
                        authService!!.refreshToken(RefreshTokenRequest(refreshToken))
                    }

                    runBlocking {
                        tokenManager.saveAccessToken(result.accessToken)
                        tokenManager.saveRefreshToken(result.refreshToken)
                    }

                    Log.d("TokenRefreshInterceptor", "Token refreshed successfully")

                    response.close()

                    val retryRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer ${result.accessToken}")
                        .build()

                    return chain.proceed(retryRequest)
                } catch (e: Exception) {
                    Log.e("TokenRefreshInterceptor", "Token refresh failed: ${e.message}")

                    if (e is HttpException && e.code() == 401) {
                        Log.e("TokenRefreshInterceptor", "Refresh token expired, clearing tokens")
                        runBlocking {
                            tokenManager.clearToken()
                        }
                    }
                }
            }
        }

        return response
    }
}
