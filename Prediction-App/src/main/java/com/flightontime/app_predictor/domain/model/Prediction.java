package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record Prediction(
        Long id,
        Long flightRequestId,
        String predictedStatus,
        Double predictedProbability,
        String modelVersion,
        OffsetDateTime predictedAt,
        OffsetDateTime createdAt
) {
}
