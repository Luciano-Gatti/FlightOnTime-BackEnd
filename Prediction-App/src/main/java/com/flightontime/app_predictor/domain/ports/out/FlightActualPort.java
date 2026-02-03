package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Interfaz FlightActualPort.
 */
public interface FlightActualPort {
    Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate);

    Optional<FlightActualResult> fetchByRouteAndWindow(
            String originIata,
            String destIata,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd
    );
}
