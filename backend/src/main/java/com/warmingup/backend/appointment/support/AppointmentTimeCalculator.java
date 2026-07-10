package com.warmingup.backend.appointment.support;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AppointmentTimeCalculator {

    public AppointmentTimeCalculation calculate(
            LocalDateTime arrivalTime,
            int travelMinutes,
            int bufferMinutes,
            List<Integer> stepDurations
    ) {
        Objects.requireNonNull(arrivalTime, "arrivalTime must not be null");
        validateNonNegative("travelMinutes", travelMinutes);
        validateNonNegative("bufferMinutes", bufferMinutes);

        List<Integer> durations = stepDurations == null ? List.of() : stepDurations;
        for (int index = 0; index < durations.size(); index++) {
            validateNonNegative("stepDurations[" + index + "]", durations.get(index));
        }

        LocalDateTime departureTime = arrivalTime
                .minusMinutes(bufferMinutes)
                .minusMinutes(travelMinutes);
        int totalPrepMinutes = durations.stream()
                .mapToInt(Integer::intValue)
                .sum();
        LocalDateTime preparationStartTime = departureTime.minusMinutes(totalPrepMinutes);

        List<StepTimeline> stepTimelines = new ArrayList<>();
        LocalDateTime cursor = preparationStartTime;
        for (int index = 0; index < durations.size(); index++) {
            LocalDateTime startTime = cursor;
            LocalDateTime endTime = startTime.plusMinutes(durations.get(index));
            stepTimelines.add(new StepTimeline(index, startTime, endTime));
            cursor = endTime;
        }

        return new AppointmentTimeCalculation(
                preparationStartTime,
                departureTime,
                List.copyOf(stepTimelines)
        );
    }

    private void validateNonNegative(String fieldName, Integer value) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
    }
}
