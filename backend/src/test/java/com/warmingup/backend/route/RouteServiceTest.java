package com.warmingup.backend.route;

import com.warmingup.backend.route.client.GoogleRouteEta;
import com.warmingup.backend.route.client.GoogleRoutesClient;
import com.warmingup.backend.route.dto.CoordinateRequest;
import com.warmingup.backend.route.dto.RouteEtaRequest;
import com.warmingup.backend.route.dto.RouteEtaResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RouteServiceTest {

    @Test
    void calculatesEtaWithFormattedTexts() {
        RouteService routeService = new RouteService(new StubGoogleRoutesClient(1840, 12300));
        RouteEtaRequest request = new RouteEtaRequest(
                new CoordinateRequest(37.5665, 126.9780),
                new CoordinateRequest(37.4979, 127.0276)
        );

        RouteEtaResponse response = routeService.calculateEta(request);

        assertAll(
                () -> assertThat(response.durationSeconds()).isEqualTo(1840),
                () -> assertThat(response.distanceMeters()).isEqualTo(12300),
                () -> assertThat(response.durationText()).isEqualTo("31분"),
                () -> assertThat(response.distanceText()).isEqualTo("12.3km")
        );
    }

    @Test
    void formatsDurationOverOneHour() {
        RouteService routeService = new RouteService(new StubGoogleRoutesClient(5520, 800));
        RouteEtaRequest request = new RouteEtaRequest(
                new CoordinateRequest(37.5665, 126.9780),
                new CoordinateRequest(37.4979, 127.0276)
        );

        RouteEtaResponse response = routeService.calculateEta(request);

        assertAll(
                () -> assertThat(response.durationText()).isEqualTo("1시간 32분"),
                () -> assertThat(response.distanceText()).isEqualTo("800m")
        );
    }

    private record StubGoogleRoutesClient(long durationSeconds, int distanceMeters) implements GoogleRoutesClient {

        @Override
        public GoogleRouteEta calculateEta(CoordinateRequest origin, CoordinateRequest destination) {
            return new GoogleRouteEta(durationSeconds, distanceMeters);
        }
    }
}
