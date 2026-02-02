package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record PredictionAccuracySample(
        OffsetDateTime flightDateUtc,
        OffsetDateTime predictedAt,
        String predictedStatus,
        String actualStatus
) {
}
