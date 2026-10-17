package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.data.AppGraph
import com.example.data.KolabRepository
import com.kolab.data.TokenManager
import com.kolab.data.api.ApiClient
import com.kolab.data.chat.ChatSocketManager

class KolabApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val tokenManager = TokenManager(this)
        ApiClient.initialize(tokenManager, BuildConfig.BASE_URL)

        val socketManager = ChatSocketManager(tokenManager)

        AppGraph.applicationContext = applicationContext
        AppGraph.tokenManager = tokenManager
        AppGraph.socketManager = socketManager
        AppGraph.repository = KolabRepository(this, ApiClient.getApi(), tokenManager, socketManager)
    }

    /**
     * Single, app-wide Coil loader. Enabling crossfade means every AsyncImage
     * fades in as it decodes instead of snapping into place — a small but
     * consistent bit of polish that pairs with the shimmer placeholders.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .build()
}