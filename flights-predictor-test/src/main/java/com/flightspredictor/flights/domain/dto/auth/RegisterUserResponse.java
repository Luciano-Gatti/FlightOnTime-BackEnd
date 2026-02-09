package com.flightspredictor.flights.domain.dto.auth;

import java.util.List;

public record RegisterUserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        List<String> roles
) {
}
