package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro UserPrediction.
 */
public record UserPrediction(
        Long id,
        Long userId,
        Long flightRequestId,
        Long flightPredictionId,
        UserPredictionSource source,
        OffsetDateTime createdAt
) {
}
