package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightSubscriptionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Interfaz FlightSubscriptionJpaRepository.
 */
public interface FlightSubscriptionJpaRepository extends JpaRepository<FlightSubscriptionEntity, Long> {
    Optional<FlightSubscriptionEntity> findFirstByUserIdAndFlightRequestId(Long userId, Long flightRequestId);

    @Query("""
            select follow
            from FlightSubscriptionEntity follow
            join FlightRequestEntity request
              on request.id = follow.flightRequestId
            where follow.refreshMode = :refreshMode
              and request.flightDateUtc between :start and :end
              and request.active = true
            """)
    List<FlightSubscriptionEntity> findByRefreshModeAndFlightDateBetween(
            @Param("refreshMode") RefreshMode refreshMode,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            select follow
            from FlightSubscriptionEntity follow
            join FlightRequestEntity request
              on request.id = follow.flightRequestId
            where request.flightDateUtc between :start and :end
            """)
    List<FlightSubscriptionEntity> findByFlightDateBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
