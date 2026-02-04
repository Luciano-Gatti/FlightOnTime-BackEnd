package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro UserAuthData.
 * @param id variable de entrada id.
 * @param email variable de entrada email.
 * @param firstName variable de entrada firstName.
 * @param lastName variable de entrada lastName.
 * @param roles variable de entrada roles.
 * @param createdAt variable de entrada createdAt.
 * @param passwordHash variable de entrada passwordHash.
 * @return resultado de la operación resultado.
 */
public record UserAuthData(
        Long id,
        String email,
        String firstName,
        String lastName,
        String roles,
        OffsetDateTime createdAt,
        String passwordHash
) {
}
