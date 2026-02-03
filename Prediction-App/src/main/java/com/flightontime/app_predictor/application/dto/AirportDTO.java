package com.flightontime.app_predictor.application.dto;

import com.flightontime.app_predictor.domain.model.Airport;

/**
 * Registro AirportDTO.
 */
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
    public static AirportDTO fromDomain(Airport airport) {
        return new AirportDTO(
                airport.airportIata(),
                airport.airportName(),
                airport.country(),
                airport.cityName(),
                airport.latitude(),
                airport.longitude(),
                airport.elevation(),
                airport.timeZone(),
                airport.googleMaps()
        );
    }
}
