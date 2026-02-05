package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

/**
 * Interfaz FlightRequestPopularityView.
 */
public interface FlightRequestPopularityView {
    /**
     * Ejecuta la operación get flight request id.
     * @return resultado de la operación get flight request id.
     */
    Long getFlightRequestId();

    /**
     * Ejecuta la operación get unique users.
     * @return resultado de la operación get unique users.
     */

    long getUniqueUsers();
}
