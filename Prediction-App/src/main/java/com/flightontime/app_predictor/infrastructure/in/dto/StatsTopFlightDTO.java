package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record StatsTopFlightDTO(
        Long requestId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime flightDate,
        String carrier,
        String origin,
        String destination,
        String flightNumber,
        long uniqueUsersCount
) {
}
