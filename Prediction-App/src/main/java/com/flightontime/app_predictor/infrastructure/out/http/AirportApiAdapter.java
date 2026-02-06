package com.flightontime.app_predictor.infrastructure.out.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiItem;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Adapter HTTP para consulta de aeropuertos.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "false", matchIfMissing = true)
public class AirportApiAdapter implements AirportInfoPort {
    private static final Logger log = LoggerFactory.getLogger(AirportApiAdapter.class);
    private static final String PROVIDER_NAME = "airport-api";

    private final AirportApiClient airportApiClient;
    private final ObjectMapper objectMapper;

    public AirportApiAdapter(AirportApiClient airportApiClient, ObjectMapper objectMapper) {
        this.airportApiClient = airportApiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        log.info("Calling provider for iata={}", airportIata);
        try {
            AirportApiResponse response = airportApiClient.fetchAirportByIata(airportIata);
            validateResponse(response, airportIata);
            AirportApiItem item = toItem(response);
            Airport airport = toDomain(item);
            log.info("provider found=true iata={}", airportIata);
            return Optional.of(airport);
        } catch (WebClientResponseException.NotFound ex) {
            log.info("provider found=false iata={}", airportIata);
            return Optional.empty();
        } catch (WebClientResponseException ex) {
            throw new ExternalProviderException(
                    PROVIDER_NAME,
                    ex.getStatusCode().value(),
                    "Airport API error for iata=" + airportIata,
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (WebClientRequestException ex) {
            throw new ExternalProviderException(
                    PROVIDER_NAME,
                    503,
                    "Airport API unavailable for iata=" + airportIata,
                    null,
                    ex
            );
        } catch (Exception ex) {
            int fallbackStatus = ex instanceof ReadTimeoutException ? 504 : 502;
            throw new ExternalProviderException(
                    PROVIDER_NAME,
                    fallbackStatus,
                    "Airport API parse/unexpected error for iata=" + airportIata,
                    null,
                    ex
            );
        }
    }

    @Override
    public List<Airport> searchByText(String text) {
        return Collections.emptyList();
    }

    private AirportApiItem toItem(AirportApiResponse response) {
        return new AirportApiItem(
                response.airportIata(),
                response.airportName(),
                response.country() != null ? response.country().name() : null,
                response.cityName(),
                response.location() != null ? response.location().lat() : null,
                response.location() != null ? response.location().lon() : null,
                response.elevation() != null ? response.elevation().meter() : null,
                response.timeZone(),
                response.urls() != null ? response.urls().googleMaps() : null
        );
    }

    private Airport toDomain(AirportApiItem item) {
        return new Airport(
                item.airportIata(),
                item.airportName(),
                item.country(),
                item.cityName(),
                item.latitude(),
                item.longitude(),
                item.elevation(),
                item.timeZone(),
                item.googleMaps()
        );
    }

    private void validateResponse(AirportApiResponse response, String airportIata) {
        List<String> missingFields = new ArrayList<>();
        if (response == null) {
            missingFields.add("response");
        } else {
            if (isBlank(response.airportIata())) {
                missingFields.add("iata");
            }
            if (isBlank(response.airportName())) {
                missingFields.add("fullName");
            }
            if (response.country() == null || isBlank(response.country().name())) {
                missingFields.add("country.name");
            }
            if (isBlank(response.cityName())) {
                missingFields.add("municipalityName");
            }
            if (response.location() == null || response.location().lat() == null) {
                missingFields.add("location.lat");
            }
            if (response.location() == null || response.location().lon() == null) {
                missingFields.add("location.lon");
            }
            if (isBlank(response.timeZone())) {
                missingFields.add("timeZone");
            }
        }

        if (!missingFields.isEmpty()) {
            log.warn("Airport provider payload invalid iata={} missingFields={}", airportIata, missingFields);
            logJson("Airport provider payload iata=" + airportIata, response);
            throw new ExternalProviderException(
                    PROVIDER_NAME,
                    502,
                    "Airport provider payload invalid for iata=" + airportIata,
                    toJson(response),
                    null
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

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

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "<failed to serialize payload>";
        }
    }
}
