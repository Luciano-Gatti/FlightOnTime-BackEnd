package com.flightontime.app_predictor.infrastructure.out.dto;

import java.time.OffsetDateTime;

public record WeatherApiResponse(
        double temp,
        double windSpeed,
        double visibility,
        boolean precipitationFlag,
        OffsetDateTime updatedAt
) {
}
