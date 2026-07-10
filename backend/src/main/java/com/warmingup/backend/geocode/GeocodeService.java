package com.warmingup.backend.geocode;

import com.warmingup.backend.geocode.client.GoogleGeocodeResult;
import com.warmingup.backend.geocode.client.GoogleGeocodingClient;
import com.warmingup.backend.geocode.dto.GeocodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeocodeService {

    private final GoogleGeocodingClient googleGeocodingClient;

    public GeocodeResponse geocode(String address) {
        GoogleGeocodeResult result = googleGeocodingClient.geocode(address);

        return new GeocodeResponse(
                result.latitude(),
                result.longitude(),
                result.formattedAddress()
        );
    }
}
