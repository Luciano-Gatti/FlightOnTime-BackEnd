package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Airport;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz AirportRepositoryPort.
 */
public interface AirportRepositoryPort {
    /**
     * Ejecuta la operación find by iata.
     * @param airportIata variable de entrada airportIata.
     * @return resultado de la operación find by iata.
     */
    Optional<Airport> findByIata(String airportIata);

    /**
     * Ejecuta la operación save.
     * @param airport variable de entrada airport.
     * @return resultado de la operación save.
     */
    Airport save(Airport airport);

    /**
     * Ejecuta la operación save all.
     * @param airports variable de entrada airports.
     * @return resultado de la operación save all.
     */

    List<Airport> saveAll(List<Airport> airports);
}
