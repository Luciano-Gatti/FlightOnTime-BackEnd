package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz FlightRequestRepositoryPort.
 */
public interface FlightRequestRepositoryPort {
    FlightRequest save(FlightRequest request);

    Optional<FlightRequest> findById(Long id);

    Optional<FlightRequest> findByFlight(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata
    );

    List<FlightRequest> findByUserId(Long userId);

    List<FlightRequest> findByIds(List<Long> ids);

    List<FlightRequest> findByFlightDateBetweenWithUserPredictions(
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<FlightRequest> findByFlightDateBetweenWithoutActuals(
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<FlightRequest> findByFlightDateBeforeAndActive(OffsetDateTime cutoff);
}
