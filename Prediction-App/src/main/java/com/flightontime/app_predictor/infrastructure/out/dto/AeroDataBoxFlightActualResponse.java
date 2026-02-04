package com.flightontime.app_predictor.infrastructure.out.dto;

import java.time.OffsetDateTime;

/**
 * Registro AeroDataBoxFlightActualResponse.
 * @param status variable de entrada status.
 * @param statusCode variable de entrada statusCode.
 * @param actualDeparture variable de entrada actualDeparture.
 * @param actualArrival variable de entrada actualArrival.
 * @param departure variable de entrada departure.
 * @param arrival variable de entrada arrival.
 * @return resultado de la operación resultado.
 */
public record AeroDataBoxFlightActualResponse(
        String status,
        String statusCode,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival,
        OffsetDateTime departure,
        OffsetDateTime arrival
) {
}
