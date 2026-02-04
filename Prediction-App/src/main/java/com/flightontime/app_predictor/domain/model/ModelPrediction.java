package com.flightontime.app_predictor.domain.model;

/**
 * Registro ModelPrediction.
 * @param predictedStatus variable de entrada predictedStatus.
 * @param predictedProbability variable de entrada predictedProbability.
 * @param confidence variable de entrada confidence.
 * @param thresholdUsed variable de entrada thresholdUsed.
 * @param modelVersion variable de entrada modelVersion.
 * @return resultado de la operación resultado.
 */
public record ModelPrediction(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion
) {
}
