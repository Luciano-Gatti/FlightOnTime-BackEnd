package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictHistoryPrediction.
 * @param predictedStatus variable de entrada predictedStatus.
 * @param predictedProbability variable de entrada predictedProbability.
 * @param confidence variable de entrada confidence.
 * @param thresholdUsed variable de entrada thresholdUsed.
 * @param modelVersion variable de entrada modelVersion.
 * @param predictedAt variable de entrada predictedAt.
 * @return resultado de la operación resultado.
 */
public record PredictHistoryPrediction(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        OffsetDateTime predictedAt
) {
}
