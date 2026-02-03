package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

/**
 * Registro PredictCommand.
 */
public record PredictCommand(
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber
) {
}
