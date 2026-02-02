package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record FlightActualResult(
        String actualStatus,
        OffsetDateTime actualDeparture,
        OffsetDateTime actualArrival
) {
}
