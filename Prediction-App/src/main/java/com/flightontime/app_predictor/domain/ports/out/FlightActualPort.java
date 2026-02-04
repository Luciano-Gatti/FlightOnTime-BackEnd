package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Interfaz FlightActualPort.
 */
public interface FlightActualPort {
    /**
     * Ejecuta la operación fetch by flight number.
     * @param flightNumber variable de entrada flightNumber.
     * @param flightDate variable de entrada flightDate.
     * @return resultado de la operación fetch by flight number.
     */
    Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate);

    /**
     * Ejecuta la operación fetch by route and window.
     * @param originIata variable de entrada originIata.
     * @param destIata variable de entrada destIata.
     * @param windowStart variable de entrada windowStart.
     * @param windowEnd variable de entrada windowEnd.
     * @return resultado de la operación fetch by route and window.
     */

    Optional<FlightActualResult> fetchByRouteAndWindow(
            String originIata,
            String destIata,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd
    );
}
