package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightRequest.
 */
public record FlightRequest(
        Long id,
        Long userId,
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        double distance,
        String flightNumber,
        OffsetDateTime createdAt,
        boolean active,
        OffsetDateTime closedAt
) {
}
