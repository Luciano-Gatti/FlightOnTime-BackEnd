package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PredictionRepositoryPort {
    Prediction save(Prediction prediction);

    Optional<Prediction> findById(Long id);

    Optional<Prediction> findByRequestIdAndForecastBucketUtc(
            Long flightRequestId,
            OffsetDateTime forecastBucketUtc
    );

    List<Prediction> findByRequestIdAndUserId(Long flightRequestId, Long userId);

    long countAll();

    long countByStatus(String predictedStatus);

    List<PredictionAccuracySample> findAccuracySamplesExcludingCancelled();
}
