package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro Prediction.
 * @param id variable de entrada id.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param forecastBucketUtc variable de entrada forecastBucketUtc.
 * @param predictedStatus variable de entrada predictedStatus.
 * @param predictedProbability variable de entrada predictedProbability.
 * @param confidence variable de entrada confidence.
 * @param thresholdUsed variable de entrada thresholdUsed.
 * @param modelVersion variable de entrada modelVersion.
 * @param source variable de entrada source.
 * @param predictedAt variable de entrada predictedAt.
 * @param createdAt variable de entrada createdAt.
 * @return resultado de la operación resultado.
 */
public record Prediction(
        Long id,
        Long flightRequestId,
        OffsetDateTime forecastBucketUtc,
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        PredictionSource source,
        OffsetDateTime predictedAt,
        OffsetDateTime createdAt
) {
}
