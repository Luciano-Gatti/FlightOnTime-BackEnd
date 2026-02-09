package com.flightspredictor.flights.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotBlank
        @Size(max = 100)
        String firstName,
        @NotBlank
        @Size(max = 100)
        String lastName
) {
}
