package com.flightspredictor.flights.domain.mapper.prediction;

import com.flightspredictor.flights.domain.enum.Prevision;
import com.flightspredictor.flights.domain.enum.Status;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.dto.prediction.PredictionResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionMapperTest {

    private final PredictionMapper mapper = new PredictionMapper();

    @Test
    void mapToModelResponseMarksDelayedAndCritical() {
        PredictionResponse response = new PredictionResponse("Delayed", 0.9, "Alta");

        ModelPredictionResponse result = mapper.mapToModelResponse(response);

        assertThat(result.prevision()).isEqualTo(Prevision.DELAYED);
        assertThat(result.probability()).isEqualTo(0.9);
        assertThat(result.status()).isEqualTo(Status.HIGH);
    }

    @Test
    void mapToModelResponseMarksOnTimeAndSuccess() {
        PredictionResponse response = new PredictionResponse("Puntual", 0.2, "Baja");

        ModelPredictionResponse result = mapper.mapToModelResponse(response);

        assertThat(result.prevision()).isEqualTo(Prevision.ON_TIME);
        assertThat(result.probability()).isEqualTo(0.2);
        assertThat(result.status()).isEqualTo(Status.LOW);
    }
}
