package com.example.warming_up.data.routine

class RoutineRepository(
    private val apiClient: RoutineApiClient = RoutineApiClient(),
) {
    suspend fun getRoutines(): Result<List<Routine>> {
        return runCatching { apiClient.fetchRoutines() }
    }
}
