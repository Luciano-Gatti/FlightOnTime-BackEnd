package com.flightontime.app_predictor.domain.model;

public record FlightRequestPopularity(
        Long requestId,
        long uniqueUsers
) {
}
