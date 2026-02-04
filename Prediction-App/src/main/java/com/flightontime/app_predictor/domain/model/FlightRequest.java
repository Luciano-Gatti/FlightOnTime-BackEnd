package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightRequest.
 * @param id variable de entrada id.
 * @param userId variable de entrada userId.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param distance variable de entrada distance.
 * @param flightNumber variable de entrada flightNumber.
 * @param createdAt variable de entrada createdAt.
 * @param active variable de entrada active.
 * @param closedAt variable de entrada closedAt.
 * @return resultado de la operación resultado.
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
