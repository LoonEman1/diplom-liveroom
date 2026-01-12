package com.example.liveroom.di

import android.content.Context
import com.example.liveroom.data.remote.api.AuthService
import com.example.liveroom.data.remote.api.ServerApiService
import com.example.liveroom.data.repository.ServerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient : OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://194.226.49.64:8080/")
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