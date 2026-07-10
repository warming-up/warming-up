package com.example.warming_up.data.appointment

import com.example.warming_up.network.AppointmentApi
import com.example.warming_up.network.AppointmentCreateRequest
import com.example.warming_up.network.AppointmentResponse
import java.io.IOException

class AppointmentRepository(
    private val appointmentApi: AppointmentApi,
) {
    suspend fun createAppointment(
        routineId: Long,
        name: String,
        arrivalTime: String,
        travelMinutes: Int,
        bufferMinutes: Int,
    ): Result<Appointment> {
        return runCatching {
            val response = appointmentApi.create(
                AppointmentCreateRequest(
                    routineId = routineId,
                    name = name,
                    arrivalTime = arrivalTime,
                    travelMinutes = travelMinutes,
                    bufferMinutes = bufferMinutes,
                ),
            )

            if (!response.isSuccessful) {
                throw AppointmentCreateException(response.code())
            }

            response.body()?.toAppointment()
                ?: throw IOException("Appointment create response body is empty.")
        }
    }

    suspend fun getAppointment(appointmentId: Long): Result<Appointment> {
        return runCatching {
            val response = appointmentApi.getAppointment(appointmentId)

            if (!response.isSuccessful) {
                throw AppointmentGetException(response.code())
            }

            response.body()?.toAppointment()
                ?: throw IOException("Appointment get response body is empty.")
        }
    }
}

class AppointmentCreateException(
    val statusCode: Int,
) : Exception("Appointment create failed with status $statusCode.")

class AppointmentGetException(
    val statusCode: Int,
) : Exception("Appointment get failed with status $statusCode.")

private fun AppointmentResponse.toAppointment(): Appointment {
    return Appointment(
        id = id,
        name = name,
        preparationStartTime = preparationStartTime,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        steps = steps.map {
            AppointmentStep(
                id = it.id,
                name = it.name,
                durationMinutes = it.durationMinutes,
                itemOrder = it.itemOrder,
                startTime = it.startTime,
                endTime = it.endTime,
                completed = it.completed,
            )
        }.sortedBy { it.itemOrder },
        checklist = checklist.map {
            AppointmentChecklistItem(
                id = it.id,
                name = it.name,
                itemOrder = it.itemOrder,
                completed = it.completed,
            )
        }.sortedBy { it.itemOrder },
    )
}
