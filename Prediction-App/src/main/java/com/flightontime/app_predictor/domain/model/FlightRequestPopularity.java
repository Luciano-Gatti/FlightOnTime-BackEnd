package com.flightontime.app_predictor.domain.model;

/**
 * Registro FlightRequestPopularity.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param uniqueUsers variable de entrada uniqueUsers.
 * @return resultado de la operación resultado.
 */
public record FlightRequestPopularity(
        Long flightRequestId,
        long uniqueUsers
) {
}
