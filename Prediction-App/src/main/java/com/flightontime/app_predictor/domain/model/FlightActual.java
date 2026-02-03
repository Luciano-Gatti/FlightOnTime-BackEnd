package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightActual.
 */
public record FlightActual(
        Long id,
        Long flightRequestId,
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival,
        String actualStatus,
        OffsetDateTime createdAt
) {
}
