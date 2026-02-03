package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.PredictFlightRequest;
import com.flightontime.app_predictor.domain.model.PredictionResult;

/**
 * Interfaz PredictFlightUseCase.
 */
public interface PredictFlightUseCase {
    PredictionResult predict(PredictFlightRequest request, Long userId);

    PredictionResult getLatestPrediction(Long requestId, Long userId);
}
