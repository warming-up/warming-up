package com.example.warming_up.data.route

class GeocodeRepository(
    private val apiClient: GeocodeApiClient = GeocodeApiClient(),
) {
    suspend fun geocode(address: String): Result<GeocodeResult> {
        return runCatching { apiClient.fetchGeocode(address) }
    }
}
