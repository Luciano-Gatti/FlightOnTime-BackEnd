package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRequestJpaRepository extends JpaRepository<FlightRequestEntity, Long> {
    Optional<FlightRequestEntity> findFirstByUserIdAndFlightDateAndCarrierAndOriginAndDestinationAndFlightNumber(
            Long userId,
            OffsetDateTime flightDate,
            String carrier,
            String origin,
            String destination,
            String flightNumber
    );

    List<FlightRequestEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
