package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;

/**
 * Interfaz ModelPredictionPort.
 */
public interface ModelPredictionPort {
    /**
     * Ejecuta la operación request prediction.
     * @param command variable de entrada command.
     * @return resultado de la operación request prediction.
     */
    ModelPrediction requestPrediction(PredictFlightCommand command);
}
