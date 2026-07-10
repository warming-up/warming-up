package com.warmingup.backend.appointment.support;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentTimeCalculation(
        LocalDateTime preparationStartTime,
        LocalDateTime departureTime,
        List<StepTimeline> stepTimelines
) {
}
