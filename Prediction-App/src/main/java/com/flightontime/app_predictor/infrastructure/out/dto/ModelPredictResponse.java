package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ModelPredictResponse(
        @JsonProperty("status")
        String predictedStatus,
        @JsonProperty("probability")
        double predictedProbability,
        String modelVersion
) {
}
