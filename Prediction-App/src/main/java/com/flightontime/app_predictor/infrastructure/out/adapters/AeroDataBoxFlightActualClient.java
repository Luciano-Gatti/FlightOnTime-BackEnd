package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AeroDataBoxFlightActualResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;

@Component
public class AeroDataBoxFlightActualClient implements FlightActualPort {
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
            return parseFlightActualResponse(response);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }

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
            return parseFlightActualResponse(response);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }

    private Optional<FlightActualResult> parseFlightActualResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Optional.empty();
        }
        JsonParser parser = JsonParserFactory.getJsonParser();
        Object parsed = parsePayload(parser, responseBody);
        if (parsed instanceof List<?> list) {
            if (list.isEmpty()) {
                return Optional.empty();
            }
            Object first = list.getFirst();
            if (first instanceof Map<?, ?> map) {
                return toFlightActualResult(toResponse(map));
            }
            return Optional.empty();
        }
        if (parsed instanceof Map<?, ?> map) {
            return toFlightActualResult(toResponse(map));
        }
        return Optional.empty();
    }

    private Object parsePayload(JsonParser parser, String responseBody) {
        String trimmed = responseBody.trim();
        if (trimmed.startsWith("[")) {
            return parser.parseList(trimmed);
        }
        if (trimmed.startsWith("{")) {
            return parser.parseMap(trimmed);
        }
        return Collections.emptyList();
    }

    private Optional<FlightActualResult> toFlightActualResult(AeroDataBoxFlightActualResponse response) {
        if (response == null) {
            return Optional.empty();
        }
        String status = response.status() != null ? response.status() : response.statusCode();
        OffsetDateTime actualDeparture = firstNonNull(response.actualDeparture(), response.departure());
        OffsetDateTime actualArrival = firstNonNull(response.actualArrival(), response.arrival());
        return Optional.of(new FlightActualResult(status, actualDeparture, actualArrival));
    }

    private OffsetDateTime firstNonNull(OffsetDateTime primary, OffsetDateTime fallback) {
        return primary != null ? primary : fallback;
    }

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

    private OffsetDateTime readOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
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

    private String readString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text == null || text.isBlank() ? null : text;
    }
}
