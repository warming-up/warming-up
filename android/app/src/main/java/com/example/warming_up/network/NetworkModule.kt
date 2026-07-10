package com.example.warming_up.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BaseUrl = "https://warming-up-production.up.railway.app/"

    @Volatile
    private var authApi: AuthApi? = null

    @Volatile
    private var routineApi: RoutineApi? = null

    fun authApi(context: Context): AuthApi {
        return authApi ?: synchronized(this) {
            authApi ?: buildApi(context.applicationContext, AuthApi::class.java)
                .also { authApi = it }
        }
    }

    fun routineApi(context: Context): RoutineApi {
        return routineApi ?: synchronized(this) {
            routineApi ?: buildApi(context.applicationContext, RoutineApi::class.java)
                .also { routineApi = it }
        }
    }

    private fun <T> buildApi(context: Context, service: Class<T>): T {
        val client = OkHttpClient.Builder()
            .cookieJar(SessionCookieJar(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)
    }
}
