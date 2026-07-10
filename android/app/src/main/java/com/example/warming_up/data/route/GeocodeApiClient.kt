package com.example.warming_up.data.route

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GeocodeApiClient(
    private val baseUrl: String = RoutesApiClient.DEFAULT_BASE_URL,
) {
    suspend fun fetchGeocode(address: String): GeocodeResult = withContext(Dispatchers.IO) {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val connection = URL("$baseUrl/api/geocode?address=$encodedAddress").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5_000
        connection.readTimeout = 8_000
        connection.setRequestProperty("Accept", "application/json")

        try {
            val statusCode = connection.responseCode
            val body = connection.readBody(statusCode)

            if (statusCode !in 200..299) {
                throw GeocodeApiException(statusCode, body.ifBlank { "주소를 찾지 못했습니다." })
            }

            JSONObject(body).toGeocodeResult()
        } finally {
            connection.disconnect()
        }
    }

    private fun HttpURLConnection.readBody(statusCode: Int): String {
        val stream = if (statusCode in 200..299) inputStream else errorStream
        if (stream == null) return ""

        return BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
    }

    private fun JSONObject.toGeocodeResult(): GeocodeResult {
        return GeocodeResult(
            coordinate = Coordinate(latitude = getDouble("latitude"), longitude = getDouble("longitude")),
            formattedAddress = getString("formattedAddress"),
        )
    }
}

data class GeocodeResult(
    val coordinate: Coordinate,
    val formattedAddress: String,
)

class GeocodeApiException(
    val statusCode: Int,
    message: String,
) : RuntimeException(message)
