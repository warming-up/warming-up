package com.warmingup.backend.appointment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        Long routineId,
        @NotBlank String name,
        @NotNull LocalDateTime arrivalTime,
        @Min(0) int travelMinutes,
        @Min(0) int bufferMinutes
) {
}
