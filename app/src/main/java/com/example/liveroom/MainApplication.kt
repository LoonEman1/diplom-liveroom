package com.example.liveroom

import android.app.Application
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.liveroom.data.factory.CoilImageLoaderFactory
import com.example.liveroom.data.webrtc.CallStateManager
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