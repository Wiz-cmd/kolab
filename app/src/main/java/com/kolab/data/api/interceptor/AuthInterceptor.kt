package com.kolab.data.api.interceptor

import com.kolab.data.TokenManager
import com.kolab.data.api.KolabApi
import com.kolab.data.api.dto.RefreshRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicReference

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()

        if (token != null) {
            val authenticated = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(authenticated)
        }

        return chain.proceed(original)
    }
}

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val baseUrl: String
) : okhttp3.Authenticator {

    private val refreshLock = Any()
    private val inflight = AtomicReference<CompletableDeferred<Boolean>?>(null)

    override fun authenticate(route: okhttp3.Route?, response: Response): Response? {
        if (response.request.header("Authorization") == null) return null
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null
        if (response.request.header("X-Retry") == "true") {
            tokenManager.clearTokens()
            return null
        }

        val refreshed = refreshNow()
        if (!refreshed) return null

        val newToken = tokenManager.getAccessToken() ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .header("X-Retry", "true")
            .build()
    }

    private fun refreshNow(): Boolean {
        synchronized(refreshLock) {
            inflight.get()?.let { return runBlocking { it.await() } }

            val deferred = CompletableDeferred<Boolean>()
            if (!inflight.compareAndSet(null, deferred)) {
                return runBlocking { inflight.get()!!.await() }
            }

            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    deferred.complete(false)
                    return false
                }

                val refreshRetrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val refreshApi = refreshRetrofit.create(KolabApi::class.java)

                val result = runBlocking {
                    try {
                        val response = refreshApi.refreshToken(RefreshRequest(refreshToken))
                        if (response.isSuccessful && response.body() != null) {
                            val tokens = response.body()!!
                            tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                            true
                        } else {
                            tokenManager.clearTokens()
                            false
                        }
                    } catch (e: Exception) {
                        tokenManager.clearTokens()
                        false
                    }
                }

                deferred.complete(result)
                return result
            } finally {
                inflight.set(null)
            }
        }
    }
}
