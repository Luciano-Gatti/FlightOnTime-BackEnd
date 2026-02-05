package com.flightontime.app_predictor.infrastructure.out.http;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Clase FastApiModelClient.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "false", matchIfMissing = true)
public class FastApiModelClient implements ModelPredictionPort {
    private static final Logger log = LoggerFactory.getLogger(FastApiModelClient.class);
    private final WebClient modelWebClient;
    private final ObjectMapper objectMapper;

    public FastApiModelClient(@Qualifier("modelWebClient") WebClient modelWebClient, ObjectMapper objectMapper) {
        this.modelWebClient = modelWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Ejecuta la operación request prediction.
     * @param command variable de entrada command.
     * @return resultado de la operación request prediction.
     */
    @Override
    public ModelPrediction requestPrediction(PredictFlightCommand command) {
        int schedMinuteOfDay = (command.flightDateUtc().getHour() * 60) + command.flightDateUtc().getMinute();
        ModelPredictRequest request = new ModelPredictRequest(
                command.flightDateUtc().getYear(),
                command.flightDateUtc().getMonthValue(),
                command.flightDateUtc().getDayOfMonth(),
                command.flightDateUtc().getDayOfWeek().getValue(),
                command.flightDateUtc().getHour(),
                command.flightDateUtc().getMinute(),
                schedMinuteOfDay,
                command.airlineCode(),
                command.originIata(),
                command.destIata(),
                command.distance(),
                0.0,
                0.0,
                0.0,
                0.0,
                command.distance(),
                0.0,
                0.0
        );
        logJson("Model request payload", request);
        try {
            ModelPredictResponse response = modelWebClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ModelPredictResponse.class)
                    .block();
            ModelPredictResponse safeResponse = Objects.requireNonNull(response, "Model response is required");
            logJson("Model response payload", safeResponse);
            return new ModelPrediction(
                    normalizeStatus(safeResponse.predictedStatus()),
                    safeResponse.predictedProbability(),
                    safeResponse.confidence(),
                    safeResponse.thresholdUsed(),
                    safeResponse.modelVersion()
            );
        } catch (WebClientResponseException ex) {
            ExternalProviderException providerException = new ExternalProviderException(
                    "model-api",
                    ex.getStatusCode().value(),
                    "Model API error while requesting prediction",
                    ex.getResponseBodyAsString(),
                    ex
            );
            log.error("Model API error provider={} status={} body={}",
                    providerException.getProvider(),
                    providerException.getStatusCode(),
                    providerException.getBodyTruncated());
            throw providerException;
        }
    }

    /**
     * Ejecuta la operación normalize status.
     * @param status variable de entrada status.
     * @return resultado de la operación normalize status.
     */

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if ("PUNTUAL".equals(normalized) || "ON TIME".equals(normalized) || "ONTIME".equals(normalized)) {
            return "ON_TIME";
        }
        if ("RETRASADO".equals(normalized) || "DELAYED".equals(normalized)) {
            return "DELAYED";
        }
        return normalized.replace(' ', '_');
    }

    /**
     * Ejecuta la operación log json.
     * @param message variable de entrada message.
     * @param payload variable de entrada payload.
     */

    private void logJson(String message, Object payload) {
        try {
            log.info("{}: {}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{}: <failed to serialize payload>", message, ex);
        }
    }
}
