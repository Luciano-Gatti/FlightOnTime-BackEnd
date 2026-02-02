package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ModelPredictResponse(
        @JsonAlias({"status", "prediction"})
        String predictedStatus,
        @JsonProperty("probability")
        double predictedProbability,
        String modelVersion
) {
}
