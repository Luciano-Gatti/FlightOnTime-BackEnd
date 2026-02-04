package com.flightontime.app_predictor.infrastructure.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Registro LoginRequestDTO.
 * @param email variable de entrada email.
 * @param password variable de entrada password.
 * @return resultado de la operación resultado.
 */
public record LoginRequestDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}
