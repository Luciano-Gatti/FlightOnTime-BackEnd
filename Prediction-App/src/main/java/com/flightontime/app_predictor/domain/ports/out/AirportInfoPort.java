package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Airport;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz AirportInfoPort.
 */
public interface AirportInfoPort {
    /**
     * Ejecuta la operación find by iata.
     * @param airportIata variable de entrada airportIata.
     * @return resultado de la operación find by iata.
     */
    Optional<Airport> findByIata(String airportIata);

    /**
     * Ejecuta la operación search by text.
     * @param text variable de entrada text.
     * @return resultado de la operación search by text.
     */

    List<Airport> searchByText(String text);
}
