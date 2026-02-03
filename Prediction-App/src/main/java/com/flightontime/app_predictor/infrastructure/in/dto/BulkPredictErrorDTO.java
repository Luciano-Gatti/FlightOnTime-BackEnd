package com.flightontime.app_predictor.infrastructure.in.dto;

/**
 * Registro BulkPredictErrorDTO.
 */
public record BulkPredictErrorDTO(
        int rowNumber,
        String message,
        String rawRow
) {
}
