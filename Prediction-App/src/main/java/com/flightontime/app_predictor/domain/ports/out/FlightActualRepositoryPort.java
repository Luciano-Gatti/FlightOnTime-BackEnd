package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightActual;
import java.util.Optional;

/**
 * Interfaz FlightActualRepositoryPort.
 */
public interface FlightActualRepositoryPort {
    FlightActual save(FlightActual flightActual);

    Optional<FlightActual> findById(Long id);

    long countAll();

    long countByActualStatus(String actualStatus);
}
