package com.warmingup.backend.appointment.dto;

import com.warmingup.backend.domain.Appointment;
import com.warmingup.backend.domain.ItemType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record AppointmentResponse(
        Long id,
        String name,
        LocalDateTime preparationStartTime,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        List<AppointmentStepResponse> steps,
        List<AppointmentChecklistResponse> checklist
) {

    public static AppointmentResponse from(Appointment appointment) {
        List<AppointmentStepResponse> steps = appointment.getItems().stream()
                .filter(item -> item.getItemType() == ItemType.STEP)
                .sorted(Comparator.comparingInt(item -> item.getItemOrder()))
                .map(item -> new AppointmentStepResponse(
                        item.getId(),
                        item.getName(),
                        item.getDurationMinutes(),
                        item.getItemOrder(),
                        item.getStartTime(),
                        item.getEndTime(),
                        item.isCompleted()
                ))
                .toList();

        List<AppointmentChecklistResponse> checklist = appointment.getItems().stream()
                .filter(item -> item.getItemType() == ItemType.CHECKLIST)
                .sorted(Comparator.comparingInt(item -> item.getItemOrder()))
                .map(item -> new AppointmentChecklistResponse(
                        item.getId(),
                        item.getName(),
                        item.getItemOrder(),
                        item.isCompleted()
                ))
                .toList();

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getName(),
                appointment.getPreparationStartTime(),
                appointment.getDepartureTime(),
                appointment.getArrivalTime(),
                steps,
                checklist
        );
    }
}
