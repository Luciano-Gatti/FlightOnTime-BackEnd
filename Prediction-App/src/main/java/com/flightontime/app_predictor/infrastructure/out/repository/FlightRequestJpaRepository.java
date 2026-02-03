package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Interfaz FlightRequestJpaRepository.
 */
public interface FlightRequestJpaRepository extends JpaRepository<FlightRequestEntity, Long> {
    Optional<FlightRequestEntity> findFirstByFlightDateUtcAndAirlineCodeAndOriginIataAndDestIata(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata
    );

    @Query("""
            select distinct request
            from FlightRequestEntity request
            join UserPredictionSnapshotEntity userPrediction
              on userPrediction.flightRequestId = request.id
            where userPrediction.userId = :userId
            order by request.createdAt desc
            """)
    List<FlightRequestEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
            select request
            from FlightRequestEntity request
            where request.flightDateUtc between :start and :end
              and request.active = true
              and exists (
                select 1
                from UserPredictionSnapshotEntity userPrediction
                where userPrediction.flightRequestId = request.id
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
