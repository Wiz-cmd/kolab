package com.example

import android.app.Application
import com.example.data.AppGraph
import com.example.data.KolabRepository
import com.kolab.data.TokenManager
import com.kolab.data.api.ApiClient
import com.kolab.data.chat.ChatSocketManager

class KolabApplication : Application() {

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
}