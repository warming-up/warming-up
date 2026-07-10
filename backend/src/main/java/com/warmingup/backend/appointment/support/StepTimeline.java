package com.warmingup.backend.appointment.support;

import java.time.LocalDateTime;

public record StepTimeline(
        int stepIndex,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
