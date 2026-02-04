package com.flightontime.app_predictor.application.dto;

import java.util.List;

/**
 * Registro BulkPredictResult.
 * @param accepted variable de entrada accepted.
 * @param rejected variable de entrada rejected.
 * @param errors variable de entrada errors.
 * @return resultado de la operación resultado.
 */
public record BulkPredictResult(
        int accepted,
        int rejected,
        List<BulkPredictError> errors
) {
}
