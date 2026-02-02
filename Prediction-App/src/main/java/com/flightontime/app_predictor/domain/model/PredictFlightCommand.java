package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictFlightCommand(
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        double distance
) {
}
