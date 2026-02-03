package com.flightontime.app_predictor.domain.model;

/**
 * Registro FlightRequestPopularity.
 */
public record FlightRequestPopularity(
        Long flightRequestId,
        long uniqueUsers
) {
}
