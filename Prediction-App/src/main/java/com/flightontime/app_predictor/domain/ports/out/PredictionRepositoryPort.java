package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PredictionRepositoryPort {
    Prediction save(Prediction prediction);

    Optional<Prediction> findById(Long id);

    Optional<Prediction> findByRequestIdAndPredictedAtBetween(
            Long requestId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Prediction> findByRequestIdAndUserId(Long requestId, Long userId);

    long countAll();

    long countByStatus(String status);

    List<PredictionAccuracySample> findAccuracySamplesExcludingCancelled();
}
