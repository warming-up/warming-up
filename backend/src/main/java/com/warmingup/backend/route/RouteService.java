package com.warmingup.backend.route;

import com.warmingup.backend.route.client.GoogleRouteEta;
import com.warmingup.backend.route.client.GoogleRoutesClient;
import com.warmingup.backend.route.dto.RouteEtaRequest;
import com.warmingup.backend.route.dto.RouteEtaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final GoogleRoutesClient googleRoutesClient;

    public RouteEtaResponse calculateEta(RouteEtaRequest request) {
        GoogleRouteEta eta = googleRoutesClient.calculateEta(request.origin(), request.destination());

        return new RouteEtaResponse(
                eta.durationSeconds(),
                eta.distanceMeters(),
                formatDuration(eta.durationSeconds()),
                formatDistance(eta.distanceMeters())
        );
    }

    private String formatDuration(long durationSeconds) {
        long minutes = Math.round(durationSeconds / 60.0);
        if (minutes < 60) {
            return minutes + "분";
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + "시간";
        }
        return hours + "시간 " + remainingMinutes + "분";
    }

    private String formatDistance(int distanceMeters) {
        if (distanceMeters < 1000) {
            return distanceMeters + "m";
        }
        return String.format(Locale.US, "%.1fkm", distanceMeters / 1000.0);
    }
}
