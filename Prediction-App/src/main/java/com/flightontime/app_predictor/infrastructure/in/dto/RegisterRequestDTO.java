package com.flightontime.app_predictor.infrastructure.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Registro RegisterRequestDTO.
 */
public record RegisterRequestDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName
) {
}
