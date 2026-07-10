package com.warmingup.backend.geocode.client;

public interface GoogleGeocodingClient {

    GoogleGeocodeResult geocode(String address);
}
