package com.example.liveroom

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.liveroom.data.factory.CoilImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoaderFactory: CoilImageLoaderFactory

    override fun newImageLoader(): ImageLoader {
        return imageLoaderFactory.newImageLoader()
    }
}