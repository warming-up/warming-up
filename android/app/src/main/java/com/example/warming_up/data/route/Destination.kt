package com.example.warming_up.data.route

data class Destination(
    val name: String,
    val coordinate: Coordinate,
)

val DESTINATION_PRESETS = listOf(
    Destination(name = "강남 오피스", coordinate = Coordinate(latitude = 37.4979, longitude = 127.0276)),
    Destination(name = "집", coordinate = Coordinate(latitude = 37.5665, longitude = 126.9780)),
)
