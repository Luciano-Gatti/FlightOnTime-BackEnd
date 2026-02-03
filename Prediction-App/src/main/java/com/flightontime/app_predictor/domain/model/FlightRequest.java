package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

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
