package com.flightontime.app_predictor.application.dto;

/**
 * Registro BulkPredictError.
 * @param rowNumber variable de entrada rowNumber.
 * @param message variable de entrada message.
 * @param rawRow variable de entrada rawRow.
 * @return resultado de la operación resultado.
 */
public record BulkPredictError(
        int rowNumber,
        String message,
        String rawRow
) {
}
