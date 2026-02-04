package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro ModelPredictResponse.
 */
public record ModelPredictResponse(
        @JsonAlias({"status", "prediction", "prevision"})
        String predictedStatus,
        @JsonAlias({"probability", "probabilidad"})
        Double predictedProbability,
        @JsonAlias({"confianza", "confidence"})
        String confidence,
        String modelVersion,
        @JsonAlias({"detalles", "details"})
        ModelPredictDetails details
) {
    /**
     * Ejecuta la operación threshold used.
     * @return resultado de la operación threshold used.
     */
    public Double thresholdUsed() {
        if (details == null) {
            return null;
        }
        return details.thresholdUsed();
    }

/**
 * Registro ModelPredictDetails.
 */
    public record ModelPredictDetails(
            @JsonAlias({"umbral_usado", "threshold_used", "thresholdUsed"})
            Double thresholdUsed
    ) {
    }
}
