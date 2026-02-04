package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro StatsTopFlight.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param flightNumber variable de entrada flightNumber.
 * @param uniqueUsersCount variable de entrada uniqueUsersCount.
 * @return resultado de la operación resultado.
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
