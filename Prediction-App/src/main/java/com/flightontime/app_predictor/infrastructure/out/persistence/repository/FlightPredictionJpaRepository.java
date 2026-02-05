package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightPredictionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Interfaz FlightPredictionJpaRepository.
 */
public interface FlightPredictionJpaRepository extends JpaRepository<FlightPredictionEntity, Long> {
    /**
     * Ejecuta la operación find by flight request id and forecast bucket utc.
     * @param flightRequestId variable de entrada flightRequestId.
     * @param forecastBucketUtc variable de entrada forecastBucketUtc.
     * @return resultado de la operación find by flight request id and forecast bucket utc.
     */
    Optional<FlightPredictionEntity> findByFlightRequestIdAndForecastBucketUtc(
            Long flightRequestId,
            OffsetDateTime forecastBucketUtc
    );

    @Query("""
            select prediction
            from FlightPredictionEntity prediction
            join UserPredictionSnapshotEntity userPrediction
              on userPrediction.flightPredictionId = prediction.id
            where userPrediction.flightRequestId = :flightRequestId
              and userPrediction.userId = :userId
            order by prediction.predictedAt desc
            """)
    List<FlightPredictionEntity> findByFlightRequestIdAndUserId(
            @Param("flightRequestId") Long flightRequestId,
            @Param("userId") Long userId
    );

    /**
     * Ejecuta la operación count by predicted status.
     * @param predictedStatus variable de entrada predictedStatus.
     * @return resultado de la operación count by predicted status.
     */

    long countByPredictedStatus(String predictedStatus);

    @Query("""
            select request.flightDateUtc as flightDateUtc,
                   prediction.forecastBucketUtc as forecastBucketUtc,
                   prediction.predictedStatus as predictedStatus,
                   actual.actualStatus as actualStatus
            from FlightPredictionEntity prediction
            join FlightRequestEntity request
              on prediction.flightRequestId = request.id
            join FlightOutcomeEntity actual
              on actual.flightRequestId = request.id
            where actual.actualStatus <> 'CANCELLED'
            """)
    /**
     * Ejecuta la operación find accuracy samples excluding cancelled.
     * @return resultado de la operación find accuracy samples excluding cancelled.
     */
    List<PredictionAccuracyView> findAccuracySamplesExcludingCancelled();
}
