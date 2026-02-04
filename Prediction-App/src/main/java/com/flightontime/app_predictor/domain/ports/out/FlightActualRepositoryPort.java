package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightActual;
import java.util.Optional;

/**
 * Interfaz FlightActualRepositoryPort.
 */
public interface FlightActualRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param flightActual variable de entrada flightActual.
     * @return resultado de la operación save.
     */
    FlightActual save(FlightActual flightActual);

    /**
     * Ejecuta la operación upsert by flight request id.
     * @param flightActual variable de entrada flightActual.
     * @return resultado de la operación upsert by flight request id.
     */
    FlightActual upsertByFlightRequestId(FlightActual flightActual);

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */

    Optional<FlightActual> findById(Long id);

    /**
     * Ejecuta la operación count all.
     * @return resultado de la operación count all.
     */

    long countAll();

    /**
     * Ejecuta la operación count by actual status.
     * @param actualStatus variable de entrada actualStatus.
     * @return resultado de la operación count by actual status.
     */

    long countByActualStatus(String actualStatus);
}
