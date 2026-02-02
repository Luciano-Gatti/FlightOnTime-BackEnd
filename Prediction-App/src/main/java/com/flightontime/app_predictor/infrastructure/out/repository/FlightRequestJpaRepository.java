package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightRequestJpaRepository extends JpaRepository<FlightRequestEntity, Long> {
    Optional<FlightRequestEntity> findFirstByUserIdAndFlightDateUtcAndAirlineCodeAndOriginIataAndDestIataAndFlightNumber(
            Long userId,
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata,
            String flightNumber
    );

    List<FlightRequestEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            select request
            from FlightRequestEntity request
            where request.flightDateUtc between :start and :end
              and request.active = true
              and exists (
                select 1
                from UserPredictionSnapshotEntity userPrediction
                join FlightPredictionEntity prediction
                  on userPrediction.flightPredictionId = prediction.id
                where prediction.flightRequestId = request.id
              )
            """)
    List<FlightRequestEntity> findByFlightDateBetweenWithUserPredictions(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            select request
            from FlightRequestEntity request
            where request.flightDateUtc between :start and :end
              and request.active = true
              and not exists (
                select 1
                from FlightOutcomeEntity actual
                where actual.flightRequestId = request.id
              )
            """)
    List<FlightRequestEntity> findByFlightDateBetweenWithoutActuals(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
            select request
            from FlightRequestEntity request
            where request.flightDateUtc < :cutoff
              and request.active = true
            """)
    List<FlightRequestEntity> findByFlightDateBeforeAndActive(@Param("cutoff") OffsetDateTime cutoff);
}
