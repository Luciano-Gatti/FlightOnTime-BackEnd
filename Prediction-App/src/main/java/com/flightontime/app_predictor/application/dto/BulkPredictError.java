package com.flightontime.app_predictor.application.dto;

public record BulkPredictError(
        int rowNumber,
        String message,
        String rawRow
) {
}
