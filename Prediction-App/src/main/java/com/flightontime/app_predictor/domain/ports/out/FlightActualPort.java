package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface FlightActualPort {
    Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate);

    Optional<FlightActualResult> fetchByRouteAndWindow(
            String origin,
            String destination,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd
    );
}
