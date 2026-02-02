package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.infrastructure.out.dto.ModelPredictRequest;
import com.flightontime.app_predictor.infrastructure.out.dto.ModelPredictResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FastApiModelClient implements ModelPredictionPort {
    private static final Logger log = LoggerFactory.getLogger(FastApiModelClient.class);
    private final WebClient modelWebClient;
    private final ObjectMapper objectMapper;

    public FastApiModelClient(@Qualifier("modelWebClient") WebClient modelWebClient, ObjectMapper objectMapper) {
        this.modelWebClient = modelWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ModelPrediction requestPrediction(PredictFlightCommand command) {
        ModelPredictRequest request = new ModelPredictRequest(
                command.flightDateUtc(),
                command.airlineCode(),
                command.originIata(),
                command.destIata(),
                command.flightNumber(),
                command.distance()
        );
        logJson("Model request payload", request);
        ModelPredictResponse response = modelWebClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ModelPredictResponse.class)
                .block();
        ModelPredictResponse safeResponse = Objects.requireNonNull(response, "Model response is required");
        logJson("Model response payload", safeResponse);
        return new ModelPrediction(
                safeResponse.predictedStatus(),
                safeResponse.predictedProbability(),
                safeResponse.modelVersion()
        );
    }

    private void logJson(String message, Object payload) {
        try {
            log.info("{}: {}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{}: <failed to serialize payload>", message, ex);
        }
    }
}
