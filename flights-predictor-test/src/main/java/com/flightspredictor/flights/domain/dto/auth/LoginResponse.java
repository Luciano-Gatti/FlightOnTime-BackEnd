package com.flightspredictor.flights.domain.dto.auth;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInMinutes
) {
}
