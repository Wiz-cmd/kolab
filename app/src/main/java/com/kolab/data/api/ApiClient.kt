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

    private var retrofit: Retrofit? = null
    private var api: KolabApi? = null

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    fun initialize(tokenManager: TokenManager, baseUrl: String) {
        val authInterceptor = AuthInterceptor { tokenManager.getAccessToken() }
        val authenticator = TokenAuthenticator(tokenManager, baseUrl)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
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
