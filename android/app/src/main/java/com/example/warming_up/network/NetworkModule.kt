package com.example.warming_up.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BaseUrl = "https://warming-up-production.up.railway.app/"

    @Volatile
    private var authApi: AuthApi? = null

    fun authApi(context: Context): AuthApi {
        return authApi ?: synchronized(this) {
            authApi ?: buildAuthApi(context.applicationContext).also { authApi = it }
        }
    }

    private fun buildAuthApi(context: Context): AuthApi {
        val client = OkHttpClient.Builder()
            .cookieJar(SessionCookieJar(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
