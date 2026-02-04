package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

public record PredictCommand(
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber
) {
}
