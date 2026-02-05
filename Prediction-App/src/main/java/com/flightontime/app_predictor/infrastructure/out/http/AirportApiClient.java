package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Clase AirportApiClient.
 */
@Component
public class AirportApiClient implements AirportInfoPort {
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
    @Override
    public Optional<Airport> findByIata(String airportIata) {
        try {
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
            return Optional.ofNullable(response).map(this::toDomain);
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Airport not found in external API iata={}", airportIata);
            return Optional.empty();
        } catch (WebClientResponseException ex) {
            log.error("Airport API error status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
            throw ex;
        }
    }

    /**
     * Ejecuta la operación search by text.
     * @param text variable de entrada text.
     * @return resultado de la operación search by text.
     */
    @Override
    public List<Airport> searchByText(String text) {
        return Collections.emptyList();
    }

    /**
     * Ejecuta la operación to domain.
     * @param response variable de entrada response.
     * @return resultado de la operación to domain.
     */

    private Airport toDomain(AirportApiResponse response) {
        return new Airport(
                response.airportIata(),
                response.airportName(),
                response.country() != null ? response.country().name() : null,
                response.cityName(),
                response.location() != null ? response.location().lat() : null,
                response.location() != null ? response.location().lon() : null,
                response.elevation() != null ? response.elevation().meter() : null,
                response.timeZone(),
                response.googleMaps() != null ? response.googleMaps().googleMaps() : null
        );
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
