package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * Registro PredictResponseDTO.
 */
public record PredictResponseDTO(
        @JsonProperty("status")
        @Schema(description = "Estado previsto del vuelo", example = "ON_TIME")
        String predictedStatus,
        @JsonProperty("probability")
        @Schema(description = "Probabilidad del estado previsto", example = "0.82")
        Double predictedProbability,
        @JsonProperty("confidence")
        @Schema(description = "Nivel de confianza del modelo", example = "HIGH")
        String confidence,
        @JsonProperty("thresholdUsed")
        @Schema(description = "Umbral utilizado para la clasificación", example = "0.65")
        Double thresholdUsed,
        @Schema(description = "Versión del modelo", example = "v1.3.2")
        String modelVersion,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(description = "Fecha/hora UTC de la predicción", example = "2025-05-01T10:00:00Z")
        OffsetDateTime predictedAt
) {
}
