package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictionAccuracySample.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param forecastBucketUtc variable de entrada forecastBucketUtc.
 * @param predictedStatus variable de entrada predictedStatus.
 * @param actualStatus variable de entrada actualStatus.
 * @return resultado de la operación resultado.
 */
public record PredictionAccuracySample(
        OffsetDateTime flightDateUtc,
        OffsetDateTime forecastBucketUtc,
        String predictedStatus,
        String actualStatus
) {
}
