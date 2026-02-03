package com.flightontime.app_predictor.infrastructure.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Registro LoginRequestDTO.
 */
public record LoginRequestDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}
