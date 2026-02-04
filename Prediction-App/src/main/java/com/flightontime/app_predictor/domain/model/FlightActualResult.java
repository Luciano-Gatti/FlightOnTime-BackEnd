package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightActualResult.
 * @param actualStatus variable de entrada actualStatus.
 * @param actualDeparture variable de entrada actualDeparture.
 * @param actualArrival variable de entrada actualArrival.
 * @return resultado de la operación resultado.
 */
public record FlightActualResult(
        String actualStatus,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival
) {
}
