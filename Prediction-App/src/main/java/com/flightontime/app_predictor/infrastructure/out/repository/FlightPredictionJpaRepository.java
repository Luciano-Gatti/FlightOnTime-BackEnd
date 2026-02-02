package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightPredictionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightPredictionJpaRepository extends JpaRepository<FlightPredictionEntity, Long> {
    Optional<FlightPredictionEntity> findFirstByFlightRequestIdAndPredictedAtBetweenOrderByPredictedAtDesc(
            Long flightRequestId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    @Query("""
            select prediction
            from FlightPredictionEntity prediction
            join UserPredictionSnapshotEntity userPrediction
              on userPrediction.flightPredictionId = prediction.id
            where prediction.flightRequestId = :flightRequestId
              and userPrediction.userId = :userId
            order by prediction.predictedAt desc
            """)
    List<FlightPredictionEntity> findByFlightRequestIdAndUserId(
            @Param("flightRequestId") Long flightRequestId,
            @Param("userId") Long userId
    );

    long countByPredictedStatus(String predictedStatus);

    @Query("""
            select request.flightDateUtc as flightDateUtc,
                   prediction.predictedAt as predictedAt,
                   prediction.predictedStatus as predictedStatus,
                   actual.actualStatus as actualStatus
            from FlightPredictionEntity prediction
            join FlightRequestEntity request
              on prediction.flightRequestId = request.id
            join FlightOutcomeEntity actual
              on actual.flightRequestId = request.id
            where actual.actualStatus <> 'CANCELLED'
            """)
    List<PredictionAccuracyView> findAccuracySamplesExcludingCancelled();
}
