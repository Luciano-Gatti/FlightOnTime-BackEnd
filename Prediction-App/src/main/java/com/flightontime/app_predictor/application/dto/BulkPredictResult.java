package com.flightontime.app_predictor.application.dto;

import java.util.List;

public record BulkPredictResult(
        int accepted,
        int rejected,
        List<BulkPredictError> errors
) {
}
