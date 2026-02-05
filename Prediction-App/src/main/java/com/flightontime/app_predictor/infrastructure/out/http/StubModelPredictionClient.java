package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub del proveedor de predicciones para desarrollo local.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "true")
public class StubModelPredictionClient implements ModelPredictionPort {

    @Override
    public ModelPrediction requestPrediction(PredictFlightCommand command) {
        boolean delayed = command != null && command.flightDateUtc() != null
                && command.flightDateUtc().getHour() % 2 == 0;
        return new ModelPrediction(
                delayed ? "DELAYED" : "ON_TIME",
                delayed ? 0.72 : 0.81,
                delayed ? "MEDIUM" : "HIGH",
                0.5,
                "stub-v1"
        );
    }
}
