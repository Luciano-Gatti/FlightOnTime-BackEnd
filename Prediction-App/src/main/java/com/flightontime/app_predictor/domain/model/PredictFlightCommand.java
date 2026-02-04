package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictFlightCommand.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param flightNumber variable de entrada flightNumber.
 * @param distance variable de entrada distance.
 * @return resultado de la operación resultado.
 */
public record PredictFlightCommand(
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        double distance
) {
}
