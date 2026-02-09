package com.flightspredictor.flights.domain.dto.auth;

public record RegisterResponse(
        RegisterUserResponse user,
        String token,
        String tokenType
) {
}
