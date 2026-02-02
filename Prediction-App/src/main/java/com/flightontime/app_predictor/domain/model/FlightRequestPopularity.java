package com.flightontime.app_predictor.domain.model;

public record FlightRequestPopularity(
        Long flightRequestId,
        long uniqueUsers
) {
}
