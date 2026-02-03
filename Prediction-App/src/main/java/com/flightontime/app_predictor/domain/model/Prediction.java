package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record Prediction(
        Long id,
        Long flightRequestId,
        OffsetDateTime forecastBucketUtc,
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        PredictionSource source,
        OffsetDateTime predictedAt,
        OffsetDateTime createdAt
) {
}
