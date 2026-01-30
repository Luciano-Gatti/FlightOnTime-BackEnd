package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record PredictHistoryPredictionDTO(
        String status,
        Double probability,
        String modelVersion,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime predictedAt
) {
}
