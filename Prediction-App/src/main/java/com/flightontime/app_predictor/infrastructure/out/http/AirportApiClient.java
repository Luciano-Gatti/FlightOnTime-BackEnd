package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Clase AirportApiClient.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "false", matchIfMissing = true)
public class AirportApiClient {
    private static final Logger log = LoggerFactory.getLogger(AirportApiClient.class);
    private final WebClient airportWebClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public AirportApiClient(
            @Qualifier("airportWebClient") WebClient airportWebClient,
            @Value("${api.market.key:}") String apiKey,
            ObjectMapper objectMapper
    ) {
        this.airportWebClient = airportWebClient;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    /**
     * Ejecuta la operación find by iata.
     * @param airportIata variable de entrada airportIata.
     * @return resultado de la operación find by iata.
     */
    public AirportApiResponse fetchAirportByIata(String airportIata) {
        log.info("Fetching airport from external API iata={}", airportIata);
        AirportApiResponse response = airportWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/airports/Iata/{iata}")
                        .queryParam("withRunways", "false")
                        .queryParam("withTime", "false")
                        .build(airportIata))
                .header("x-api-market-key", apiKey)
                .retrieve()
                .bodyToMono(AirportApiResponse.class)
                .block();
        logJson("Airport API response payload iata=" + airportIata, response);
        return response;
    }

    /**
     * Ejecuta la operación log json.
     * @param message variable de entrada message.
     * @param payload variable de entrada payload.
     */

    private void logJson(String message, Object payload) {
        if (payload == null) {
            log.info("{}: <null>", message);
            return;
        }
        try {
            log.info("{}: {}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{}: <failed to serialize payload>", message, ex);
        }
    }
}
