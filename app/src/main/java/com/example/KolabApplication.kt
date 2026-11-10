package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.data.AppGraph
import com.example.data.KolabRepository
import com.kolab.data.TokenManager
import com.kolab.data.api.ApiClient
import com.kolab.data.chat.ChatSocketManager
import io.sentry.android.core.SentryAndroid

class KolabApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        initCrashReporting()

        val tokenManager = TokenManager(this)
        ApiClient.initialize(tokenManager, BuildConfig.BASE_URL)

        val socketManager = ChatSocketManager(tokenManager)

        AppGraph.applicationContext = applicationContext
        AppGraph.tokenManager = tokenManager
        AppGraph.socketManager = socketManager
        AppGraph.repository = KolabRepository(this, ApiClient.getApi(), tokenManager, socketManager)
    }

    /**
     * Crash/error reporting is free on Sentry's developer plan. It stays
     * completely disabled until SENTRY_DSN is set in .env, so debug builds and
     * forks keep working with no account. A bad DSN must never crash startup.
     */
    private fun initCrashReporting() {
        val dsn = BuildConfig.SENTRY_DSN
        if (dsn.isBlank()) return

        try {
            SentryAndroid.init(this) { options ->
                options.dsn = dsn
                options.environment = if (BuildConfig.DEBUG) "debug" else "production"
                options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}"
                options.isSendDefaultPii = false
                options.isEnableAutoSessionTracking = true
                options.tracesSampleRate = if (BuildConfig.DEBUG) 0.0 else 0.1
            }
        } catch (t: Throwable) {
            // Never let observability bring the app down.
            android.util.Log.w("KolabApplication", "Sentry init failed", t)
        }
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
