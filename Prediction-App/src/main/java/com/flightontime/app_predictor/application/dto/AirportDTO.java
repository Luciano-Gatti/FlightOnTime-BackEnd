package com.flightontime.app_predictor.application.dto;

public record AirportDTO(
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
