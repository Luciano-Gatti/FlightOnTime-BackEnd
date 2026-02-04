package com.flightontime.app_predictor.application.dto;

import java.time.OffsetDateTime;

/**
 * Registro PredictCommand.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param flightNumber variable de entrada flightNumber.
 * @return resultado de la operación resultado.
 */
public record PredictCommand(
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber
) {
}
