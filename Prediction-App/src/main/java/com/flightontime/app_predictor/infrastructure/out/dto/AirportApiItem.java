package com.flightontime.app_predictor.infrastructure.out.dto;

/**
 * Registro AirportApiItem.
 */
public record AirportApiItem(
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
