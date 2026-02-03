package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Airport;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz AirportInfoPort.
 */
public interface AirportInfoPort {
    Optional<Airport> findByIata(String airportIata);

    List<Airport> searchByText(String text);
}
