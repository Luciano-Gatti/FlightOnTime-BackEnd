package com.flightontime.app_predictor.infrastructure.out.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import com.flightontime.app_predictor.infrastructure.out.http.util.AirportUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        String url = AirportUrlBuilder.buildAirportUrl(airportIata);
        log.info("Outbound request method=GET url={}", url);
        AirportApiResponse response = airportWebClient.get()
                .uri(url)
                .header("x-api-market-key", apiKey)
                .exchangeToMono(clientResponse -> {
                    int statusCode = clientResponse.statusCode().value();
                    log.info("Airport provider response status={}", statusCode);
                    if (statusCode >= 400) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    log.error("Airport provider error status={} body={}", statusCode, truncate(body));
                                    return clientResponse.createException().flatMap(Mono::error);
                                });
                    }
                    return clientResponse.bodyToMono(AirportApiResponse.class);
                })
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

    private String truncate(String body) {
        if (body == null) {
            return null;
        }
        int limit = 1000;
        if (body.length() <= limit) {
            return body;
        }
        return body.substring(0, limit) + "...(truncated)";
    }
}
