package com.flightontime.app_predictor.infrastructure.in.dto;

/**
 * Registro UserResponseDTO.
 * @param id variable de entrada id.
 * @param email variable de entrada email.
 * @param firstName variable de entrada firstName.
 * @param lastName variable de entrada lastName.
 * @param roles variable de entrada roles.
 * @return resultado de la operación resultado.
 */
public record UserResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String roles
) {
}
