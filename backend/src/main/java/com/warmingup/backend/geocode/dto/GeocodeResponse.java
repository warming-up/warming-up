package com.warmingup.backend.geocode.dto;

public record GeocodeResponse(
        Double latitude,
        Double longitude,
        String formattedAddress
) {
}
