package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictionResult(
        String predictedStatus,
        double predictedProbability,
        String modelVersion,
        OffsetDateTime predictedAt
) {
}
