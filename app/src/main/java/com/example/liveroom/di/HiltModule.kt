package com.example.liveroom.di

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.liveroom.data.factory.CoilImageLoaderFactory
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.remote.api.AuthService
import com.example.liveroom.data.remote.api.ServerApiService
import com.example.liveroom.data.remote.dto.AuthResponse
import com.example.liveroom.data.remote.dto.RefreshTokenRequest
import com.example.liveroom.data.remote.dto.toRequestBody
import com.example.liveroom.data.repository.ServerRepository
import com.example.liveroom.data.repository.TokenRepository
import com.example.liveroom.ui.viewmodel.UserViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HiltModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() : HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Singleton
    @Provides
    fun provideTokenRefreshInterceptor(tokenManager: TokenManager): TokenRefreshInterceptor {
        return TokenRefreshInterceptor(tokenManager)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenRefreshInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideImageLoaderFactory(factory: CoilImageLoaderFactory): ImageLoaderFactory = factory

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient : OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://nighthunting23.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideServerApiService(retrofit: Retrofit): ServerApiService {
        return retrofit.create(ServerApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideServerRepository(
        @ApplicationContext context: Context,
        apiService: ServerApiService
    ): ServerRepository {
        return ServerRepository(apiService, context)
    }
}