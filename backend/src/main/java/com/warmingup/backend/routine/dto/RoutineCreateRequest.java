package com.warmingup.backend.routine.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoutineCreateRequest(
        @NotBlank String name,
        List<StepRequest> steps,
        List<String> checklist
) {
    public record StepRequest(
            @NotBlank String name,
            int durationMinutes,
            int stepOrder
    ) {}
}
