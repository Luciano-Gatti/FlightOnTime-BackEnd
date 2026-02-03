package com.flightontime.app_predictor.application.dto;

/**
 * Registro BulkPredictError.
 */
public record BulkPredictError(
        int rowNumber,
        String message,
        String rawRow
) {
}
