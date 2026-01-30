package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.infrastructure.out.dto.ModelPredictRequest;
import com.flightontime.app_predictor.infrastructure.out.dto.ModelPredictResponse;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FastApiModelClient implements ModelPredictionPort {
    private final WebClient modelWebClient;

    public FastApiModelClient(@Qualifier("modelWebClient") WebClient modelWebClient) {
        this.modelWebClient = modelWebClient;
    }

    @Override
    public ModelPrediction requestPrediction(PredictFlightCommand command) {
        ModelPredictRequest request = new ModelPredictRequest(
                command.flDate(),
                command.carrier(),
                command.origin(),
                command.dest(),
                command.flightNumber()
        );
        ModelPredictResponse response = modelWebClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ModelPredictResponse.class)
                .block();
        ModelPredictResponse safeResponse = Objects.requireNonNull(response, "Model response is required");
        return new ModelPrediction(
                safeResponse.status(),
                safeResponse.probability(),
                safeResponse.modelVersion()
        );
    }
}
