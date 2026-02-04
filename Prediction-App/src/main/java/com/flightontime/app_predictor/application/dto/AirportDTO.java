package com.flightontime.app_predictor.application.dto;

import com.flightontime.app_predictor.domain.model.Airport;

/**
 * Registro AirportDTO.
 * @param airportIata variable de entrada airportIata.
 * @param airportName variable de entrada airportName.
 * @param country variable de entrada country.
 * @param cityName variable de entrada cityName.
 * @param latitude variable de entrada latitude.
 * @param longitude variable de entrada longitude.
 * @param elevation variable de entrada elevation.
 * @param timeZone variable de entrada timeZone.
 * @param googleMaps variable de entrada googleMaps.
 * @return resultado de la operación resultado.
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
