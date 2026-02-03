package com.flightontime.app_predictor.infrastructure.out.dto;

import java.time.OffsetDateTime;

/**
 * Registro AeroDataBoxFlightActualResponse.
 */
public record AeroDataBoxFlightActualResponse(
        String status,
        String statusCode,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival,
        OffsetDateTime departure,
        OffsetDateTime arrival
) {
}
