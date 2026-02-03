package com.flightontime.app_predictor.domain.model;

/**
 * Registro ModelPrediction.
 */
public record ModelPrediction(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion
) {
}
