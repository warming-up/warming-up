package com.example.warming_up.data.appointment

data class Appointment(
    val id: Long,
    val name: String,
    val preparationStartTime: String,
    val departureTime: String,
    val arrivalTime: String,
    val steps: List<AppointmentStep>,
    val checklist: List<AppointmentChecklistItem>,
)

data class AppointmentStep(
    val id: Long,
    val name: String,
    val durationMinutes: Int,
    val itemOrder: Int,
    val startTime: String,
    val endTime: String,
    val completed: Boolean,
)

data class AppointmentChecklistItem(
    val id: Long,
    val name: String,
    val itemOrder: Int,
    val completed: Boolean,
)
