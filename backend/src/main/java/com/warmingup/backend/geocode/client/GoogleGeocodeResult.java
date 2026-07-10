package com.warmingup.backend.geocode.client;

public record GoogleGeocodeResult(
        double latitude,
        double longitude,
        String formattedAddress
) {
}
