package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record User(
        Long id,
        String email,
        String firstName,
        String lastName,
        String roles,
        OffsetDateTime createdAt
) {
}
