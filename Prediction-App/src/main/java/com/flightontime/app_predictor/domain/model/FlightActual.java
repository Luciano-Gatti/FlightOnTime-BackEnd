package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record FlightActual(
        Long id,
        Long requestId,
        OffsetDateTime flightDate,
        String carrier,
        String origin,
        String destination,
        String flightNumber,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival,
        String status,
        OffsetDateTime createdAt
) {
}
