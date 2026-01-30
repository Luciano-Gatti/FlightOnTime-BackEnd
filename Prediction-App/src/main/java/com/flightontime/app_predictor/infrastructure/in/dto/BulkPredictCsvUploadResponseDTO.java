package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

public record BulkPredictCsvUploadResponseDTO(
        int totalRows,
        int acceptedRows,
        int rejectedRows,
        List<BulkPredictErrorDTO> errors
) {
}
