package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AeroDataBoxFlightActualResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Clase AeroDataBoxFlightActualClient.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "false", matchIfMissing = true)
public class AeroDataBoxFlightActualClient implements FlightActualPort {
    private static final Logger log = LoggerFactory.getLogger(AeroDataBoxFlightActualClient.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final WebClient aeroDataBoxWebClient;
    private final ObjectMapper objectMapper;

    public AeroDataBoxFlightActualClient(
            @Qualifier("aeroDataBoxWebClient") WebClient aeroDataBoxWebClient,
            ObjectMapper objectMapper
    ) {
        this.aeroDataBoxWebClient = aeroDataBoxWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Ejecuta la operación fetch by flight number.
     * @param flightNumber variable de entrada flightNumber.
     * @param flightDate variable de entrada flightDate.
     * @return resultado de la operación fetch by flight number.
     */
    @Override
    public Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate) {
        if (flightNumber == null || flightNumber.isBlank() || flightDate == null) {
            return Optional.empty();
        }
        String date = DATE_FORMAT.format(flightDate);
        try {
            String response = aeroDataBoxWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/flights/number/{flightNumber}/{date}")
                            .build(flightNumber, date))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            /**
             * Ejecuta la operación parse flight actual response.
             * @param response variable de entrada response.
             * @return resultado de la operación parse flight actual response.
             */
            return parseFlightActualResponse(response);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        } catch (WebClientResponseException ex) {
            ExternalProviderException providerException = new ExternalProviderException(
                    "aerodatabox-api",
                    ex.getStatusCode().value(),
                    "AeroDataBox error for flight number " + flightNumber + " date " + date,
                    ex.getResponseBodyAsString(),
                    ex
            );
            log.error("AeroDataBox error provider={} flightNumber={} date={} status={} body={}",
                    providerException.getProvider(),
                    flightNumber,
                    date,
                    providerException.getStatusCode(),
                    providerException.getBodyTruncated());
            throw providerException;
        }
    }

    /**
     * Ejecuta la operación fetch by route and window.
     * @param originIata variable de entrada originIata.
     * @param destIata variable de entrada destIata.
     * @param windowStart variable de entrada windowStart.
     * @param windowEnd variable de entrada windowEnd.
     * @return resultado de la operación fetch by route and window.
     */
    @Override
    public Optional<FlightActualResult> fetchByRouteAndWindow(
            String originIata,
            String destIata,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd
    ) {
        if (originIata == null || destIata == null || windowStart == null || windowEnd == null) {
            return Optional.empty();
        }
        try {
            String response = aeroDataBoxWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/flights/route/{origin}/{destination}")
                            .queryParam("from", DATE_TIME_FORMAT.format(windowStart))
                            .queryParam("to", DATE_TIME_FORMAT.format(windowEnd))
                            .build(originIata, destIata))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            /**
             * Ejecuta la operación parse flight actual response.
             * @param response variable de entrada response.
             * @return resultado de la operación parse flight actual response.
             */
            return parseFlightActualResponse(response);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        } catch (WebClientResponseException ex) {
            ExternalProviderException providerException = new ExternalProviderException(
                    "aerodatabox-api",
                    ex.getStatusCode().value(),
                    "AeroDataBox error for route " + originIata + "-" + destIata,
                    ex.getResponseBodyAsString(),
                    ex
            );
            log.error("AeroDataBox error provider={} route={}-{} status={} body={}",
                    providerException.getProvider(),
                    originIata,
                    destIata,
                    providerException.getStatusCode(),
                    providerException.getBodyTruncated());
            throw providerException;
        }
    }

    /**
     * Ejecuta la operación parse flight actual response.
     * @param responseBody variable de entrada responseBody.
     * @return resultado de la operación parse flight actual response.
     */

    private Optional<FlightActualResult> parseFlightActualResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode payload;
            if (root.isArray()) {
                if (root.isEmpty()) {
                    return Optional.empty();
                }
                payload = root.get(0);
            } else if (root.isObject()) {
                payload = root;
            } else {
                return Optional.empty();
            }
            if (!payload.isObject()) {
                return Optional.empty();
            }
            Map<?, ?> payloadMap = objectMapper.convertValue(payload, Map.class);
            return toFlightActualResult(toResponse(payloadMap));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Ejecuta la operación to flight actual result.
     * @param response variable de entrada response.
     * @return resultado de la operación to flight actual result.
     */

    private Optional<FlightActualResult> toFlightActualResult(AeroDataBoxFlightActualResponse response) {
        if (response == null) {
            return Optional.empty();
        }
        String status = response.status() != null ? response.status() : response.statusCode();
        OffsetDateTime actualDeparture = firstNonNull(response.actualDeparture(), response.departure());
        OffsetDateTime actualArrival = firstNonNull(response.actualArrival(), response.arrival());
        return Optional.of(new FlightActualResult(status, actualDeparture, actualArrival));
    }

    /**
     * Ejecuta la operación first non null.
     * @param primary variable de entrada primary.
     * @param fallback variable de entrada fallback.
     * @return resultado de la operación first non null.
     */

    private OffsetDateTime firstNonNull(OffsetDateTime primary, OffsetDateTime fallback) {
        return primary != null ? primary : fallback;
    }

    /**
     * Ejecuta la operación to response.
     * @param Map<? variable de entrada Map<?.
     * @param map variable de entrada map.
     * @return resultado de la operación to response.
     */

    private AeroDataBoxFlightActualResponse toResponse(Map<?, ?> map) {
        String status = readString(map.get("status"));
        String statusCode = readString(map.get("statusCode"));
        OffsetDateTime actualDeparture = readOffsetDateTime(map.get("actualDeparture"));
        OffsetDateTime actualArrival = readOffsetDateTime(map.get("actualArrival"));
        OffsetDateTime departure = readOffsetDateTime(map.get("departure"));
        OffsetDateTime arrival = readOffsetDateTime(map.get("arrival"));
        return new AeroDataBoxFlightActualResponse(
                status,
                statusCode,
                actualDeparture,
                actualArrival,
                departure,
                arrival
        );
    }

    /**
     * Ejecuta la operación read offset date time.
     * @param value variable de entrada value.
     * @return resultado de la operación read offset date time.
     */

    private OffsetDateTime readOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            /**
             * Ejecuta la operación parse offset date time.
             * @param text variable de entrada text.
             * @return resultado de la operación parse offset date time.
             */
            return parseOffsetDateTime(text);
        }
        if (value instanceof Map<?, ?> map) {
            OffsetDateTime utc = parseOffsetDateTime(readString(map.get("utc")));
            if (utc != null) {
                return utc;
            }
            OffsetDateTime primary = parseOffsetDateTime(readString(map.get("actualDeparture")));
            if (primary != null) {
                return primary;
            }
            OffsetDateTime arrival = parseOffsetDateTime(readString(map.get("actualArrival")));
            if (arrival != null) {
                return arrival;
            }
            OffsetDateTime departure = parseOffsetDateTime(readString(map.get("departure")));
            if (departure != null) {
                return departure;
            }
            return parseOffsetDateTime(readString(map.get("arrival")));
        }
        return null;
    }

    /**
     * Ejecuta la operación parse offset date time.
     * @param value variable de entrada value.
     * @return resultado de la operación parse offset date time.
     */

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Ejecuta la operación read string.
     * @param value variable de entrada value.
     * @return resultado de la operación read string.
     */

    private String readString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text == null || text.isBlank() ? null : text;
    }
}
