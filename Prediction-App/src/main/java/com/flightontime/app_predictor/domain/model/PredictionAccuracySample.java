package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictionAccuracySample.
 */
public record PredictionAccuracySample(
        OffsetDateTime flightDateUtc,
        OffsetDateTime forecastBucketUtc,
        String predictedStatus,
        String actualStatus
) {
}
