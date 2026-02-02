package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record FlightFollow(
        Long id,
        Long userId,
        Long flightRequestId,
        RefreshMode refreshMode,
        Long baselineFlightPredictionId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
