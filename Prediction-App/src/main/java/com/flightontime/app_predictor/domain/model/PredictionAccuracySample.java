package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictionAccuracySample(
        OffsetDateTime flightDate,
        OffsetDateTime predictedAt,
        String predictedStatus,
        String actualStatus
) {
}
