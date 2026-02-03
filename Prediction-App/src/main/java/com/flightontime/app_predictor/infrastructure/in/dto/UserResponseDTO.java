package com.flightontime.app_predictor.infrastructure.in.dto;

/**
 * Registro UserResponseDTO.
 */
public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String roles
) {
}
