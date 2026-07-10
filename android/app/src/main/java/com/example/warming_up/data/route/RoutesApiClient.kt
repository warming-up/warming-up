package com.example.warming_up.data.route

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL

class RoutesApiClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchEta(origin: Coordinate, destination: Coordinate): RouteEta = withContext(Dispatchers.IO) {
        val connection = URL("$baseUrl/api/routes/eta").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 5_000
        connection.readTimeout = 8_000
        connection.doOutput = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", "application/json")

        try {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(buildRequestBody(origin, destination).toString())
            }

            val statusCode = connection.responseCode
            val body = connection.readBody(statusCode)

            if (statusCode !in 200..299) {
                throw RoutesApiException(statusCode, body.ifBlank { "이동시간을 불러오지 못했습니다." })
            }

            JSONObject(body).toRouteEta()
        } finally {
            connection.disconnect()
        }
    }

    private fun buildRequestBody(origin: Coordinate, destination: Coordinate): JSONObject {
        return JSONObject()
            .put("origin", origin.toJson())
            .put("destination", destination.toJson())
    }

    private fun Coordinate.toJson(): JSONObject {
        return JSONObject()
            .put("latitude", latitude)
            .put("longitude", longitude)
    }

    private fun HttpURLConnection.readBody(statusCode: Int): String {
        val stream = if (statusCode in 200..299) inputStream else errorStream
        if (stream == null) return ""

        return BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
    }

    private fun JSONObject.toRouteEta(): RouteEta {
        return RouteEta(
            durationSeconds = getLong("durationSeconds"),
            distanceMeters = getInt("distanceMeters"),
            durationText = getString("durationText"),
            distanceText = getString("distanceText"),
        )
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"

        init {
            if (CookieHandler.getDefault() == null) {
                CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
            }
        }
    }
}

class RoutesApiException(
    val statusCode: Int,
    message: String,
) : RuntimeException(message)
