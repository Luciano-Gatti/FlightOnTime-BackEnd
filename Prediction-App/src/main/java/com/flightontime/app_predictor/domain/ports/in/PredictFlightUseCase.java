package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;

public interface PredictFlightUseCase {
    PredictResponseDTO predict(PredictRequestDTO request);
}
