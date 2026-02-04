package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

/**
 * Registro AirportWeatherDTO.
 * @param temp variable de entrada temp.
 * @param windSpeed variable de entrada windSpeed.
 * @param visibility variable de entrada visibility.
 * @param precipitationFlag variable de entrada precipitationFlag.
 * @param updatedAt variable de entrada updatedAt.
 * @return resultado de la operación resultado.
 */
public record AirportWeatherDTO(
        double temp,
        double windSpeed,
        double visibility,
        boolean precipitationFlag,
        OffsetDateTime updatedAt
) {
}
