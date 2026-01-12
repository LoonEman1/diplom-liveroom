package com.example.liveroom.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {
    // ViewModels автоматически инжектятся через @HiltViewModel
    // Этот module пустой, но нужен для регистрации
}