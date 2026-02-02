package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AeroDataBoxFlightActualClient implements FlightActualPort {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final WebClient aeroDataBoxWebClient;

    public AeroDataBoxFlightActualClient(
            @Qualifier("aeroDataBoxWebClient") WebClient aeroDataBoxWebClient
    ) {
        this.aeroDataBoxWebClient = aeroDataBoxWebClient;
    }

    @Override
    public Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate) {
        if (flightNumber == null || flightNumber.isBlank() || flightDate == null) {
            return Optional.empty();
        }
        String date = DATE_FORMAT.format(flightDate);
        try {
            JsonNode response = aeroDataBoxWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/flights/number/{flightNumber}/{date}")
                            .build(flightNumber, date))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
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
            JsonNode response = aeroDataBoxWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/flights/route/{origin}/{destination}")
                            .queryParam("from", DATE_TIME_FORMAT.format(windowStart))
                            .queryParam("to", DATE_TIME_FORMAT.format(windowEnd))
                            .build(originIata, destIata))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return parseFlightActualResponse(response);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }

    private Optional<FlightActualResult> parseFlightActualResponse(JsonNode response) {
        if (response == null || response.isNull()) {
            return Optional.empty();
        }
        JsonNode flightNode = response.isArray() ? response.path(0) : response;
        if (flightNode == null || flightNode.isMissingNode() || flightNode.isNull()) {
            return Optional.empty();
        }
        String status = readStatus(flightNode);
        OffsetDateTime actualDeparture = readOffsetDateTime(
                flightNode.path("actualDeparture"),
                "actualDeparture",
                "departure"
        );
        OffsetDateTime actualArrival = readOffsetDateTime(
                flightNode.path("actualArrival"),
                "actualArrival",
                "arrival"
        );
        return Optional.of(new FlightActualResult(status, actualDeparture, actualArrival));
    }

    private String readStatus(JsonNode flightNode) {
        String status = textValue(flightNode, "status");
        if (status == null) {
            status = textValue(flightNode, "statusCode");
        }
        return status;
    }

    private OffsetDateTime readOffsetDateTime(JsonNode node, String primaryField, String fallbackField) {
        String value = null;
        if (node != null && node.isObject()) {
            value = textValue(node, "utc");
            if (value == null) {
                value = textValue(node, primaryField);
            }
        }
        if (value == null) {
            value = textValue(node, fallbackField);
        }
        if (value == null) {
            return null;
        }
        return OffsetDateTime.parse(value);
    }

    private String textValue(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode child = node.get(field);
        if (child == null || child.isNull() || child.isMissingNode()) {
            return null;
        }
        String value = child.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
