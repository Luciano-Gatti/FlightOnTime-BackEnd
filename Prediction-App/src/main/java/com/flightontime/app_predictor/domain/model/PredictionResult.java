package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictionResult(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        OffsetDateTime predictedAt
) {
}
