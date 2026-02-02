package com.flightontime.app_predictor.domain.model;

public record ModelPrediction(
        String predictedStatus,
        double predictedProbability,
        String modelVersion
) {
}
