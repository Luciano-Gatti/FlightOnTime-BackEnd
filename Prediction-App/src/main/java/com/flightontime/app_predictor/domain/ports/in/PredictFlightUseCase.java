package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;

/**
 * Interfaz PredictFlightUseCase.
 */
public interface PredictFlightUseCase {
    PredictResponseDTO predict(PredictRequestDTO request, Long userId);

    PredictResponseDTO getLatestPrediction(Long requestId, Long userId);
}
