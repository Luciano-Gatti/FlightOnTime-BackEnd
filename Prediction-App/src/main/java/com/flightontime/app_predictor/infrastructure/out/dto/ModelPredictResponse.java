package com.flightontime.app_predictor.infrastructure.out.dto;

public record ModelPredictResponse(
        String status,
        double probability,
        String modelVersion
) {
}
