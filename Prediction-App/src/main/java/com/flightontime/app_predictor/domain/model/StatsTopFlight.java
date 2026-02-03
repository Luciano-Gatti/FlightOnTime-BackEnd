package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro StatsTopFlight.
 */
public record StatsTopFlight(
        Long flightRequestId,
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        long uniqueUsersCount
) {
}
