package com.example.warming_up.data.route

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

data class RouteEta(
    val durationSeconds: Long,
    val distanceMeters: Int,
    val durationText: String,
    val distanceText: String,
)
