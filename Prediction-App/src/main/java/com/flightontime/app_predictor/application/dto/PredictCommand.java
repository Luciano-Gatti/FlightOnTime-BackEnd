package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

public record PredictCommand(
        OffsetDateTime flDateUtc,
        String carrier,
        String origin,
        String dest,
        String flightNumber
) {
}
