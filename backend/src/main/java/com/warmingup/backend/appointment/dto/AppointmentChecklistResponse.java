package com.warmingup.backend.appointment.dto;

public record AppointmentChecklistResponse(
        Long id,
        String name,
        int itemOrder,
        boolean completed
) {
}
