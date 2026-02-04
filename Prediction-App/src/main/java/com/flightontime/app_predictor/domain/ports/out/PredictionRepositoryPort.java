package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz PredictionRepositoryPort.
 */
public interface PredictionRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param prediction variable de entrada prediction.
     * @return resultado de la operación save.
     */
    Prediction save(Prediction prediction);

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */

    Optional<Prediction> findById(Long id);

    /**
     * Ejecuta la operación find by request id and forecast bucket utc.
     * @param flightRequestId variable de entrada flightRequestId.
     * @param forecastBucketUtc variable de entrada forecastBucketUtc.
     * @return resultado de la operación find by request id and forecast bucket utc.
     */

    Optional<Prediction> findByRequestIdAndForecastBucketUtc(
            Long flightRequestId,
            OffsetDateTime forecastBucketUtc
    );

    /**
     * Ejecuta la operación find by request id and user id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @param userId variable de entrada userId.
     * @return resultado de la operación find by request id and user id.
     */

    List<Prediction> findByRequestIdAndUserId(Long flightRequestId, Long userId);

    /**
     * Ejecuta la operación count all.
     * @return resultado de la operación count all.
     */

    long countAll();

    /**
     * Ejecuta la operación count by status.
     * @param predictedStatus variable de entrada predictedStatus.
     * @return resultado de la operación count by status.
     */

    long countByStatus(String predictedStatus);

    /**
     * Ejecuta la operación find accuracy samples excluding cancelled.
     * @return resultado de la operación find accuracy samples excluding cancelled.
     */

    List<PredictionAccuracySample> findAccuracySamplesExcludingCancelled();
}
