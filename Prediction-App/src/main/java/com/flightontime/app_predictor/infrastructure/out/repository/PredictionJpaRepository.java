package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.PredictionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PredictionJpaRepository extends JpaRepository<PredictionEntity, Long> {
    Optional<PredictionEntity> findFirstByRequestIdAndPredictedAtBetweenOrderByPredictedAtDesc(
            Long requestId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    @Query("""
            select prediction
            from PredictionEntity prediction
            join UserPredictionEntity userPrediction
              on userPrediction.predictionId = prediction.id
            where prediction.requestId = :requestId
              and userPrediction.userId = :userId
            order by prediction.predictedAt desc
            """)
    List<PredictionEntity> findByRequestIdAndUserId(
            @Param("requestId") Long requestId,
            @Param("userId") Long userId
    );

    long countByStatus(String status);

    @Query("""
            select request.flightDate as flightDate,
                   prediction.predictedAt as predictedAt,
                   prediction.status as predictedStatus,
                   actual.status as actualStatus
            from PredictionEntity prediction
            join FlightRequestEntity request
              on prediction.requestId = request.id
            join FlightActualEntity actual
              on actual.requestId = request.id
            where actual.status <> 'CANCELLED'
            """)
    List<PredictionAccuracyView> findAccuracySamplesExcludingCancelled();
}
