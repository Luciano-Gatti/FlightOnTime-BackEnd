package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;

/**
 * Interfaz ModelPredictionPort.
 */
public interface ModelPredictionPort {
    ModelPrediction requestPrediction(PredictFlightCommand command);
}
