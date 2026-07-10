package com.warmingup.backend.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RouteEtaRequest(
        @Valid
        @NotNull(message = "출발지는 필수입니다.")
        CoordinateRequest origin,

        @Valid
        @NotNull(message = "도착지는 필수입니다.")
        CoordinateRequest destination
) {
}
