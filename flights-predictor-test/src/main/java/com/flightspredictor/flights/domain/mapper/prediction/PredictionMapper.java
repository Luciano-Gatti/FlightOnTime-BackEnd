package com.flightspredictor.flights.domain.mapper.prediction;

import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.dto.prediction.PredictionResponse;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import org.springframework.stereotype.Component;

@Component
public class PredictionMapper {

    public ModelPredictionResponse mapToModelResponse(PredictionResponse response) {

        PredictedStatus predictedStatus = setPredictedStatus(response.prevision());

        return new ModelPredictionResponse(
                predictedStatus,
                response.probabilidad(),
                response.confianza()
        );
    }

    private PredictedStatus setPredictedStatus(String prevision) {
        if ("Retrasado".equalsIgnoreCase(prevision) || "Delayed".equalsIgnoreCase(prevision)) {
            return PredictedStatus.DELAYED;
        }
        return PredictedStatus.ON_TIME;
    }
}
