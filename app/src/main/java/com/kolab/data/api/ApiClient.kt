package com.kolab.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kolab.data.TokenManager
import com.kolab.data.api.interceptor.AuthInterceptor
import com.kolab.data.api.interceptor.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Used when the build has no (or a malformed) base URL. Shipping an empty
    // BASE_URL used to crash the app in Retrofit's baseUrl() at startup.
    const val FALLBACK_BASE_URL = "https://kolab-api-7r6i.onrender.com/api/"

    private var retrofit: Retrofit? = null
    private var api: KolabApi? = null

    /**
     * Returns a URL Retrofit accepts: absolute http(s) with a host and a
     * trailing slash. Anything else falls back to production.
     */
    internal fun normalizeBaseUrl(raw: String): String {
        val candidate = raw.trim()
        val parsed = runCatching { java.net.URI(candidate) }.getOrNull()
        val usable = parsed != null &&
            (parsed.scheme == "http" || parsed.scheme == "https") &&
            !parsed.host.isNullOrBlank()
        val chosen = if (usable) candidate else FALLBACK_BASE_URL
        return if (chosen.endsWith("/")) chosen else "$chosen/"
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    fun initialize(tokenManager: TokenManager, baseUrl: String) {
        val safeBaseUrl = normalizeBaseUrl(baseUrl)
        val authInterceptor = AuthInterceptor { tokenManager.getAccessToken() }
        val authenticator = TokenAuthenticator(tokenManager, safeBaseUrl)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Never log bearer tokens / refresh tokens, even in debug builds.
            redactHeader("Authorization")
            redactHeader("Cookie")
            level = if (com.example.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .retryOnConnectionFailure(true)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(safeBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit!!.create(KolabApi::class.java)
    }

    fun getApi(): KolabApi {
        if (api == null) throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
        return api!!
    }
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>()
}

suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.code(), "Empty response body")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val message = try {
                val errorJson = Gson().fromJson(errorBody, Map::class.java)
                errorJson?.get("message") as? String ?: errorBody ?: "Unknown error"
            } catch (e: Exception) {
                errorBody ?: "Unknown error"
            }
            ApiResult.Error(response.code(), message)
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error(0, "No internet connection")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error(0, "Connection timed out")
    } catch (e: Exception) {
        ApiResult.Exception(e)
    }
}
