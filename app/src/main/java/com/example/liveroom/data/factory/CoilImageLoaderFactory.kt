package com.example.liveroom.data.factory

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilImageLoaderFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context : Context
) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(false)
            .build()
    }
}