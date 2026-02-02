package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record PredictHistoryPredictionDTO(
        @JsonProperty("status")
        String predictedStatus,
        @JsonProperty("probability")
        Double predictedProbability,
        String modelVersion,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime predictedAt
) {
}
