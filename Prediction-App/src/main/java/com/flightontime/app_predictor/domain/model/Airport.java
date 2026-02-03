package com.flightontime.app_predictor.domain.model;

/**
 * Registro Airport.
 */
public record Airport(
        String airportIata,
        String airportName,
        String country,
        String cityName,
        Double latitude,
        Double longitude,
        Double elevation,
        String timeZone,
        String googleMaps
) {
}
