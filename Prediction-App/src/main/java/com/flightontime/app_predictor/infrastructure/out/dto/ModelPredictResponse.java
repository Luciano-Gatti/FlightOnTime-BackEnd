package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ModelPredictResponse(
        @JsonAlias({"status", "prediction", "prevision"})
        String predictedStatus,
        @JsonAlias({"probability", "probabilidad"})
        Double predictedProbability,
        @JsonAlias({"confianza", "confidence"})
        String confidence,
        String modelVersion
) {
}
