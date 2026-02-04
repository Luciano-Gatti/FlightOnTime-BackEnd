package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.PredictFlightRequest;
import com.flightontime.app_predictor.domain.model.PredictionResult;

/**
 * Interfaz PredictFlightUseCase.
 */
public interface PredictFlightUseCase {
    /**
     * Ejecuta la operación predict.
     * @param request variable de entrada request.
     * @param userId variable de entrada userId.
     * @return resultado de la operación predict.
     */
    PredictionResult predict(PredictFlightRequest request, Long userId);

    /**
     * Ejecuta la operación get latest prediction.
     * @param requestId variable de entrada requestId.
     * @param userId variable de entrada userId.
     * @return resultado de la operación get latest prediction.
     */

    PredictionResult getLatestPrediction(Long requestId, Long userId);
}
