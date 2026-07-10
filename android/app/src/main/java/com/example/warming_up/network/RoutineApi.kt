package com.example.warming_up.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RoutineApi {
    @GET("api/routines")
    suspend fun getRoutines(): Response<List<RoutineResponse>>

    @POST("api/routines")
    suspend fun create(@Body request: RoutineCreateRequest): Response<RoutineResponse>
}

data class RoutineCreateRequest(
    val name: String,
    val steps: List<StepRequest>,
    val checklist: List<String>,
)

data class StepRequest(
    val name: String,
    val durationMinutes: Int,
    val stepOrder: Int,
)

data class RoutineResponse(
    val id: Long,
    val name: String,
    val totalDurationMinutes: Int,
    val steps: List<StepResponse>,
    val checklist: List<ChecklistResponse>,
    val createdAt: String,
)

data class StepResponse(
    val id: Long,
    val name: String,
    val durationMinutes: Int,
    val itemOrder: Int,
)

data class ChecklistResponse(
    val id: Long,
    val name: String,
    val itemOrder: Int,
)
