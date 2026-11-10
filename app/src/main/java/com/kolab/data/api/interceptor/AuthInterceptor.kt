package com.kolab.data.api.interceptor

import com.kolab.data.TokenManager
import com.kolab.data.api.KolabApi
import com.kolab.data.api.dto.RefreshRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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
    private var refreshInFlight: CompletableDeferred<Boolean>? = null

    /**
     * Dedicated client for token refresh. Keeping it separate from the main
     * client avoids re-entering the authenticator (which would loop) and gives
     * the refresh call its own connection pool.
     */
    private val refreshClient: KolabApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KolabApi::class.java)
    }

    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        val authorization = response.request.header("Authorization") ?: return null
        val staleToken = authorization.removePrefix("Bearer ").trim()

        // Never try to refresh the refresh call itself.
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null

        // Already retried once with a fresh token: the session is dead.
        if (response.request.header("X-Retry") == "true") {
            tokenManager.clearTokens()
            return null
        }

        // A concurrent request may already have rotated the token while this
        // one was in flight. Reuse it instead of refreshing a second time
        // (refresh tokens are single-use and a double rotation logs the user out).
        val storedToken = tokenManager.getAccessToken()
        val newToken = when {
            !storedToken.isNullOrBlank() && storedToken != staleToken -> storedToken
            refreshNow() -> tokenManager.getAccessToken()
            else -> null
        }
        if (newToken.isNullOrBlank()) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .header("X-Retry", "true")
            .build()
    }

    /**
     * Single-flight refresh: the first 401 performs the refresh, all other
     * concurrent 401s await its result instead of rotating the token again.
     */
    private fun refreshNow(): Boolean {
        val deferred: CompletableDeferred<Boolean>
        val isOwner: Boolean

        synchronized(refreshLock) {
            val existing = refreshInFlight
            if (existing != null) {
                deferred = existing
                isOwner = false
            } else {
                deferred = CompletableDeferred()
                refreshInFlight = deferred
                isOwner = true
            }
        }

        if (!isOwner) return runBlocking { deferred.await() }

        val result = try {
            doRefresh()
        } catch (e: Exception) {
            // Transient failure: keep the session so the user can retry.
            false
        } finally {
            synchronized(refreshLock) { refreshInFlight = null }
        }
        deferred.complete(result)
        return result
    }

    private fun doRefresh(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false

        return runBlocking {
            try {
                val response = refreshClient.refreshToken(RefreshRequest(refreshToken))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                    true
                } else if (response.code() == 401 || response.code() == 403) {
                    // The refresh token is no longer valid: clear the session.
                    tokenManager.clearTokens()
                    false
                } else {
                    // Server/network hiccup: don't destroy a possibly valid session.
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
