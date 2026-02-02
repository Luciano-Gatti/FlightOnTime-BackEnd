package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AeroDataBoxFlightActualResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
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
        try {
            AeroDataBoxFlightActualResponse response = objectMapper.readValue(
                    responseBody,
                    AeroDataBoxFlightActualResponse.class
            );
            return toFlightActualResult(response);
        } catch (MismatchedInputException ex) {
            return parseFlightActualResponseList(responseBody);
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private Optional<FlightActualResult> parseFlightActualResponseList(String responseBody) {
        try {
            List<AeroDataBoxFlightActualResponse> responses = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<AeroDataBoxFlightActualResponse>>() {
                    }
            );
            if (responses == null || responses.isEmpty()) {
                return Optional.empty();
            }
            return toFlightActualResult(responses.getFirst());
        } catch (IOException ex) {
            return Optional.empty();
        }
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
}
