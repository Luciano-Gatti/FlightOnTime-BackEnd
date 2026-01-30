package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRequestRepositoryPort {
    FlightRequest save(FlightRequest request);

    Optional<FlightRequest> findById(Long id);

    Optional<FlightRequest> findByUserAndFlight(
            Long userId,
            OffsetDateTime flightDate,
            String carrier,
            String origin,
            String destination,
            String flightNumber
    );

    List<FlightRequest> findByUserId(Long userId);

    List<FlightRequest> findByFlightDateBetweenWithUserPredictions(
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<FlightRequest> findByFlightDateBetweenWithoutActuals(
            OffsetDateTime start,
            OffsetDateTime end
    );
}
