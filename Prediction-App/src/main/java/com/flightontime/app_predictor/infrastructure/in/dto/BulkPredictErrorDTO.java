package com.flightontime.app_predictor.infrastructure.in.dto;

/**
 * Registro BulkPredictErrorDTO.
 * @param rowNumber variable de entrada rowNumber.
 * @param message variable de entrada message.
 * @param rawRow variable de entrada rawRow.
 * @return resultado de la operación resultado.
 */
public record BulkPredictErrorDTO(
        int rowNumber,
        String message,
        String rawRow
) {
}
