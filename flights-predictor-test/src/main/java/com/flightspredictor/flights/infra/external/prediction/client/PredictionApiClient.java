package com.flightspredictor.flights.infra.external.prediction.client;

import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionRequest;
import com.flightspredictor.flights.domain.dto.prediction.PredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PredictionApiClient {

    private final WebClient webClient;

    public PredictionResponse predict(ModelPredictionRequest request){
        return webClient
                .post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .block();
    }

}
