package com.flightontime.app_predictor.infrastructure.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Registro RegisterRequestDTO.
 * @param email variable de entrada email.
 * @param password variable de entrada password.
 * @param firstName variable de entrada firstName.
 * @param lastName variable de entrada lastName.
 * @return resultado de la operación resultado.
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
