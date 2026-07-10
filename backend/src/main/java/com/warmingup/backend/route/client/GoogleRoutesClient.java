package com.warmingup.backend.route.client;

import com.warmingup.backend.route.dto.CoordinateRequest;

public interface GoogleRoutesClient {

    GoogleRouteEta calculateEta(CoordinateRequest origin, CoordinateRequest destination);
}
