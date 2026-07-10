package com.example.warming_up.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>
}

data class LoginRequest(
    val email: String,
    val password: String,
)

data class UserResponse(
    val id: Long,
    val email: String,
)
