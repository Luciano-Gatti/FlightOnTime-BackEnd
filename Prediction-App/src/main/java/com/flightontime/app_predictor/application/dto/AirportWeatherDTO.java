package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

public record AirportWeatherDTO(
        double temp,
        double windSpeed,
        double visibility,
        boolean precipitationFlag,
        OffsetDateTime updatedAt
) {
}
