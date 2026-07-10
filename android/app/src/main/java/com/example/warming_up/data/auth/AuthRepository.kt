package com.example.warming_up.data.auth

import com.example.warming_up.network.AuthApi
import com.example.warming_up.network.LoginRequest
import com.example.warming_up.network.UserResponse
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApi,
) {
    suspend fun login(email: String, password: String): Result<UserResponse> {
        return runCatching {
            val response = authApi.login(LoginRequest(email = email, password = password))

            if (!response.isSuccessful) {
                throw LoginException(response.code())
            }

            response.body() ?: throw IOException("Login response body is empty.")
        }
    }
}

class LoginException(
    val statusCode: Int,
) : Exception("Login failed with status $statusCode.")
