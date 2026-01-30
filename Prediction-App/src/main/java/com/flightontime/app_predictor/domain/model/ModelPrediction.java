package com.flightontime.app_predictor.domain.model;

public record ModelPrediction(
        String status,
        double probability,
        String modelVersion
) {
}
