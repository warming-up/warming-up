package com.warmingup.backend.route.client;

public record GoogleRouteEta(
        long durationSeconds,
        int distanceMeters
) {
}
