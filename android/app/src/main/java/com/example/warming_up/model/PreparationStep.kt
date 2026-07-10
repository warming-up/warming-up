package com.example.warming_up.model

data class PreparationStep(
    val id: Long,
    val name: String,
    val timeText: String,
    val isRunning: Boolean,
    val isCompleted: Boolean,
)
