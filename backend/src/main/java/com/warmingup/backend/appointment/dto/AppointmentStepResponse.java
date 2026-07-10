package com.warmingup.backend.appointment.dto;

import java.time.LocalDateTime;

public record AppointmentStepResponse(
        Long id,
        String name,
        int durationMinutes,
        int itemOrder,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean completed
) {
}
