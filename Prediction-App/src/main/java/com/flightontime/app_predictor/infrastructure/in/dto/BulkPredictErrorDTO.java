package com.flightontime.app_predictor.infrastructure.in.dto;

public record BulkPredictErrorDTO(
        int rowNumber,
        String message,
        String rawRow
) {
}
