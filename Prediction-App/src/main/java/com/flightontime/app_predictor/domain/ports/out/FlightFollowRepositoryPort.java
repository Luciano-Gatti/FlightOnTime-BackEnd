package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightFollowRepositoryPort {
    FlightFollow save(FlightFollow flightFollow);

    Optional<FlightFollow> findByUserIdAndRequestId(Long userId, Long requestId);

    List<FlightFollow> findByRefreshModeAndFlightDateBetween(
            RefreshMode refreshMode,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<FlightFollow> findByFlightDateBetween(OffsetDateTime start, OffsetDateTime end);
}
