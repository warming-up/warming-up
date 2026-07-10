package com.example.warming_up.data.route

class RoutesRepository(
    private val apiClient: RoutesApiClient = RoutesApiClient(),
) {
    suspend fun getEta(origin: Coordinate, destination: Coordinate): Result<RouteEta> {
        return runCatching { apiClient.fetchEta(origin, destination) }
    }
}
