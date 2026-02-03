package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictHistoryPrediction.
 */
public record PredictHistoryPrediction(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        OffsetDateTime predictedAt
) {
}
