package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record FlightRequest(
        Long id,
        Long userId,
        OffsetDateTime flightDate,
        String carrier,
        String origin,
        String destination,
        String flightNumber,
        OffsetDateTime createdAt,
        boolean active,
        OffsetDateTime closedAt
) {
}
