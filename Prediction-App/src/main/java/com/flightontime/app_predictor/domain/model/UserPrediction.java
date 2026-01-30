package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record UserPrediction(
        Long id,
        Long userId,
        Long predictionId,
        OffsetDateTime createdAt
) {
}
