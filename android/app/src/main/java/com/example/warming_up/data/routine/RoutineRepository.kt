package com.example.warming_up.data.routine

import com.example.warming_up.network.RoutineApi
import com.example.warming_up.network.RoutineCreateRequest
import com.example.warming_up.network.RoutineResponse
import com.example.warming_up.network.StepRequest
import java.io.IOException

class RoutineRepository(
    private val routineApi: RoutineApi,
) {
    suspend fun getRoutines(): Result<List<Routine>> {
        return runCatching {
            val response = routineApi.getRoutines()

            if (!response.isSuccessful) {
                throw RoutineApiException(response.code(), "루틴을 불러오지 못했습니다.")
            }

            response.body()?.map { it.toRoutine() }
                ?: throw IOException("Routine response body is empty.")
        }
    }

    suspend fun createRoutine(
        name: String,
        steps: List<RoutineStepInput>,
        checklist: List<String>,
    ): Result<RoutineResponse> {
        return runCatching {
            val request = RoutineCreateRequest(
                name = name,
                steps = steps.mapIndexed { index, step ->
                    StepRequest(
                        name = step.name,
                        durationMinutes = step.durationMinutes,
                        stepOrder = index + 1,
                    )
                },
                checklist = checklist,
            )
            val response = routineApi.create(request)

            if (!response.isSuccessful) {
                throw RoutineCreateException(response.code())
            }

            response.body() ?: throw IOException("Routine create response body is empty.")
        }
    }
}

data class RoutineStepInput(
    val name: String,
    val durationMinutes: Int,
)

class RoutineCreateException(
    val statusCode: Int,
) : Exception("Routine create failed with status $statusCode.")

private fun RoutineResponse.toRoutine(): Routine {
    return Routine(
        id = id,
        name = name,
        totalDurationMinutes = totalDurationMinutes,
        steps = steps.map {
            RoutineStep(
                id = it.id,
                name = it.name,
                durationMinutes = it.durationMinutes,
                itemOrder = it.itemOrder,
            )
        }.sortedBy { it.itemOrder },
        checklist = checklist.map {
            RoutineChecklistItem(
                id = it.id,
                name = it.name,
                itemOrder = it.itemOrder,
            )
        }.sortedBy { it.itemOrder },
        createdAt = createdAt,
    )
}
