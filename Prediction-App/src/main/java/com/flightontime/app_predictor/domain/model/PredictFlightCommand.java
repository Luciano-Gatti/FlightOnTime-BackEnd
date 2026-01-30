package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictFlightCommand(
        OffsetDateTime flDate,
        String carrier,
        String origin,
        String dest,
        String flightNumber,
        double distance
) {
}
