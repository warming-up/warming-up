package com.warmingup.backend.route.client;

import com.warmingup.backend.route.dto.CoordinateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public class GoogleRoutesRestClient implements GoogleRoutesClient {

    private static final String FIELD_MASK = "routes.duration,routes.distanceMeters";

    private final RestClient restClient;
    private final String apiKey;

    public GoogleRoutesRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${google.maps.routes.base-url:https://routes.googleapis.com}") String baseUrl,
            @Value("${google.maps.routes.api-key:}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public GoogleRouteEta calculateEta(CoordinateRequest origin, CoordinateRequest destination) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Google Routes API 키가 설정되어 있지 않습니다.");
        }

        GoogleComputeRoutesResponse response;
        try {
            response = restClient.post()
                    .uri("/directions/v2:computeRoutes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .body(GoogleComputeRoutesRequest.from(origin, destination))
                    .retrieve()
                    .body(GoogleComputeRoutesResponse.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google Routes API 호출에 실패했습니다.");
        }

        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            throw new ResponseStatusException(BAD_GATEWAY, "이동 경로를 계산할 수 없습니다.");
        }

        GoogleRoute route = response.routes().getFirst();
        return new GoogleRouteEta(parseDurationSeconds(route.duration()), route.distanceMeters());
    }

    private long parseDurationSeconds(String duration) {
        if (!StringUtils.hasText(duration) || !duration.endsWith("s")) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google Routes API 응답의 이동시간 형식이 올바르지 않습니다.");
        }

        try {
            return Long.parseLong(duration.substring(0, duration.length() - 1));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google Routes API 응답의 이동시간 형식이 올바르지 않습니다.");
        }
    }

    private record GoogleComputeRoutesRequest(
            Waypoint origin,
            Waypoint destination,
            String travelMode,
            String routingPreference,
            String units
    ) {
        private static GoogleComputeRoutesRequest from(CoordinateRequest origin, CoordinateRequest destination) {
            return new GoogleComputeRoutesRequest(
                    Waypoint.from(origin),
                    Waypoint.from(destination),
                    "DRIVE",
                    "TRAFFIC_AWARE",
                    "METRIC"
            );
        }
    }

    private record Waypoint(Location location) {
        private static Waypoint from(CoordinateRequest coordinate) {
            return new Waypoint(new Location(new LatLng(coordinate.latitude(), coordinate.longitude())));
        }
    }

    private record Location(LatLng latLng) {
    }

    private record LatLng(Double latitude, Double longitude) {
    }

    private record GoogleComputeRoutesResponse(List<GoogleRoute> routes) {
    }

    private record GoogleRoute(String duration, int distanceMeters) {
    }
}
