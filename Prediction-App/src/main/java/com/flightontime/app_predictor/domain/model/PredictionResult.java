package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictionResult(
        String status,
        double probability,
        String modelVersion,
        OffsetDateTime predictedAt
) {
}
