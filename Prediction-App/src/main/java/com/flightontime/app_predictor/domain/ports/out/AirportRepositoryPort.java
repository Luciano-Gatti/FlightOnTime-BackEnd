package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Airport;
import java.util.Optional;

public interface AirportRepositoryPort {
    Optional<Airport> findByIataCode(String iataCode);
}
