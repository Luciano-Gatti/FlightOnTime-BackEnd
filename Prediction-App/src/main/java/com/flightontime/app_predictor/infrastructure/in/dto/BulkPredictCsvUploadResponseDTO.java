package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

/**
 * Registro BulkPredictCsvUploadResponseDTO.
 * @param totalRows variable de entrada totalRows.
 * @param acceptedRows variable de entrada acceptedRows.
 * @param rejectedRows variable de entrada rejectedRows.
 * @param errors variable de entrada errors.
 * @return resultado de la operación resultado.
 */
public record BulkPredictCsvUploadResponseDTO(
        int totalRows,
        int acceptedRows,
        int rejectedRows,
        List<BulkPredictErrorDTO> errors
) {
}
