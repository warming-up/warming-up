package com.example.warming_up.data.routine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL

class RoutineApiClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchRoutines(): List<Routine> = withContext(Dispatchers.IO) {
        val connection = URL("$baseUrl/api/routines").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        connection.setRequestProperty("Accept", "application/json")

        try {
            val statusCode = connection.responseCode
            val body = connection.readBody(statusCode)

            if (statusCode !in 200..299) {
                throw RoutineApiException(statusCode, body.ifBlank { "루틴을 불러오지 못했습니다." })
            }

            JSONArray(body).toRoutineList()
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

    private fun JSONArray.toRoutineList(): List<Routine> {
        return List(length()) { index ->
            getJSONObject(index).toRoutine()
        }
    }

    private fun JSONObject.toRoutine(): Routine {
        return Routine(
            id = getLong("id"),
            name = getString("name"),
            totalDurationMinutes = getInt("totalDurationMinutes"),
            steps = getJSONArray("steps").toStepList(),
            checklist = getJSONArray("checklist").toChecklist(),
            createdAt = optString("createdAt"),
        )
    }

    private fun JSONArray.toStepList(): List<RoutineStep> {
        return List(length()) { index ->
            val item = getJSONObject(index)
            RoutineStep(
                id = item.getLong("id"),
                name = item.getString("name"),
                durationMinutes = item.getInt("durationMinutes"),
                itemOrder = item.getInt("itemOrder"),
            )
        }.sortedBy { it.itemOrder }
    }

    private fun JSONArray.toChecklist(): List<RoutineChecklistItem> {
        return List(length()) { index ->
            val item = getJSONObject(index)
            RoutineChecklistItem(
                id = item.getLong("id"),
                name = item.getString("name"),
                itemOrder = item.getInt("itemOrder"),
            )
        }.sortedBy { it.itemOrder }
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

class RoutineApiException(
    val statusCode: Int,
    message: String,
) : RuntimeException(message)
