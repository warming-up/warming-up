package com.warmingup.backend.route.dto;

public record RouteEtaResponse(
        long durationSeconds,
        int distanceMeters,
        String durationText,
        String distanceText
) {
}
