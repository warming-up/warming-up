package com.warmingup.backend.routine.dto;

import com.warmingup.backend.domain.ItemType;
import com.warmingup.backend.domain.Routine;

import java.time.LocalDateTime;
import java.util.List;

public record RoutineResponse(
        Long id,
        String name,
        int totalDurationMinutes,
        List<StepResponse> steps,
        List<ChecklistResponse> checklist,
        LocalDateTime createdAt
) {
    public record StepResponse(Long id, String name, int durationMinutes, int itemOrder) {}
    public record ChecklistResponse(Long id, String name, int itemOrder) {}

    public static RoutineResponse from(Routine routine) {
        List<StepResponse> steps = routine.getItems().stream()
                .filter(i -> i.getItemType() == ItemType.STEP)
                .map(i -> new StepResponse(i.getId(), i.getName(), i.getDurationMinutes(), i.getItemOrder()))
                .toList();

        List<ChecklistResponse> checklist = routine.getItems().stream()
                .filter(i -> i.getItemType() == ItemType.CHECKLIST)
                .map(i -> new ChecklistResponse(i.getId(), i.getName(), i.getItemOrder()))
                .toList();

        return new RoutineResponse(
                routine.getId(),
                routine.getName(),
                routine.getTotalDurationMinutes(),
                steps,
                checklist,
                routine.getCreatedAt()
        );
    }
}
