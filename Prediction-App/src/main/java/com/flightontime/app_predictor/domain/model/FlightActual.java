package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightActual.
 * @param id variable de entrada id.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param flightNumber variable de entrada flightNumber.
 * @param actualDeparture variable de entrada actualDeparture.
 * @param actualArrival variable de entrada actualArrival.
 * @param actualStatus variable de entrada actualStatus.
 * @param createdAt variable de entrada createdAt.
 * @return resultado de la operación resultado.
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
