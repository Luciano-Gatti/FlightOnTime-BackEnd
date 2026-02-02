package com.flightontime.app_predictor.domain.model;

public record ModelPrediction(
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion
) {
}
