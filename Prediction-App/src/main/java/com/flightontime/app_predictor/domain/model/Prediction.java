package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record Prediction(
        Long id,
        Long requestId,
        String status,
        Double probability,
        String modelVersion,
        OffsetDateTime predictedAt,
        OffsetDateTime createdAt
) {
}
