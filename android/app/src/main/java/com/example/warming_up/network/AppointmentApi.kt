package com.example.warming_up.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppointmentApi {
    @POST("api/appointments")
    suspend fun create(@Body request: AppointmentCreateRequest): Response<AppointmentResponse>

    @GET("api/appointments/{appointmentId}")
    suspend fun getAppointment(
        @Path("appointmentId") appointmentId: Long,
    ): Response<AppointmentResponse>
}

data class AppointmentCreateRequest(
    val routineId: Long,
    val name: String,
    val arrivalTime: String,
    val travelMinutes: Int,
    val bufferMinutes: Int,
)

data class AppointmentResponse(
    val id: Long,
    val name: String,
    val preparationStartTime: String,
    val departureTime: String,
    val arrivalTime: String,
    val steps: List<AppointmentStepResponse>,
    val checklist: List<AppointmentChecklistResponse>,
)

data class AppointmentStepResponse(
    val id: Long,
    val name: String,
    val durationMinutes: Int,
    val itemOrder: Int,
    val startTime: String,
    val endTime: String,
    val completed: Boolean,
)

data class AppointmentChecklistResponse(
    val id: Long,
    val name: String,
    val itemOrder: Int,
    val completed: Boolean,
)
