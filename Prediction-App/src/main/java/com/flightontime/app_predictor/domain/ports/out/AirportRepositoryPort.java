package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Airport;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz AirportRepositoryPort.
 */
public interface AirportRepositoryPort {
    Optional<Airport> findByIata(String airportIata);

    List<Airport> saveAll(List<Airport> airports);
}
