package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro UserAuthData.
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
