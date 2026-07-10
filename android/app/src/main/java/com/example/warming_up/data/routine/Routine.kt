package com.example.warming_up.data.routine

data class Routine(
    val id: Long,
    val name: String,
    val totalDurationMinutes: Int,
    val steps: List<RoutineStep>,
    val checklist: List<RoutineChecklistItem>,
    val createdAt: String,
)

data class RoutineStep(
    val id: Long,
    val name: String,
    val durationMinutes: Int,
    val itemOrder: Int,
)

data class RoutineChecklistItem(
    val id: Long,
    val name: String,
    val itemOrder: Int,
)
