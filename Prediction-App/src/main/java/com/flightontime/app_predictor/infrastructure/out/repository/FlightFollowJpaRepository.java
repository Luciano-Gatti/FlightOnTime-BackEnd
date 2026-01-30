package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightFollowEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightFollowJpaRepository extends JpaRepository<FlightFollowEntity, Long> {
    Optional<FlightFollowEntity> findFirstByUserIdAndRequestId(Long userId, Long requestId);

    @Query("""
            select follow
            from FlightFollowEntity follow
            join FlightRequestEntity request
              on request.id = follow.requestId
            where follow.refreshMode = :refreshMode
              and request.flightDate between :start and :end
            """)
    List<FlightFollowEntity> findByRefreshModeAndFlightDateBetween(
            @Param("refreshMode") RefreshMode refreshMode,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            select follow
            from FlightFollowEntity follow
            join FlightRequestEntity request
              on request.id = follow.requestId
            where request.flightDate between :start and :end
            """)
    List<FlightFollowEntity> findByFlightDateBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
