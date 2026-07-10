package com.warmingup.backend.geocode.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class GoogleGeocodingRestClient implements GoogleGeocodingClient {

    private final RestClient restClient;
    private final String apiKey;

    public GoogleGeocodingRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${google.maps.geocoding.base-url:https://maps.googleapis.com}") String baseUrl,
            @Value("${google.maps.geocoding.api-key:}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public GoogleGeocodeResult geocode(String address) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Google Geocoding API 키가 설정되어 있지 않습니다.");
        }

        GoogleGeocodingResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeocodingResponse.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google Geocoding API 호출에 실패했습니다.");
        }

        if (response == null || response.status() == null) {
            throw new ResponseStatusException(BAD_GATEWAY, "주소를 좌표로 변환할 수 없습니다.");
        }

        if ("ZERO_RESULTS".equals(response.status())) {
            throw new ResponseStatusException(NOT_FOUND, "입력한 주소로 좌표를 찾을 수 없습니다.");
        }

        if (!"OK".equals(response.status()) || response.results() == null || response.results().isEmpty()) {
            throw new ResponseStatusException(BAD_GATEWAY, "주소를 좌표로 변환할 수 없습니다.");
        }

        GeocodingResult result = response.results().getFirst();
        Location location = result.geometry().location();
        return new GoogleGeocodeResult(location.lat(), location.lng(), result.formattedAddress());
    }

    private record GoogleGeocodingResponse(List<GeocodingResult> results, String status) {
    }

    private record GeocodingResult(
            @JsonProperty("formatted_address") String formattedAddress,
            Geometry geometry
    ) {
    }

    private record Geometry(Location location) {
    }

    private record Location(double lat, double lng) {
    }
}
