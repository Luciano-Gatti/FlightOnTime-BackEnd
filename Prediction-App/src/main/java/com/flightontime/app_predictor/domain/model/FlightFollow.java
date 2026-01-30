package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record FlightFollow(
        Long id,
        Long userId,
        Long requestId,
        RefreshMode refreshMode,
        Long baselinePredictionId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
