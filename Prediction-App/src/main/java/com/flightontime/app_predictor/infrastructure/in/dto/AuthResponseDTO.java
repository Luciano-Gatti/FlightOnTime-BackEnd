package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record AuthResponseDTO(
        String token,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime expiresAt,
        UserResponseDTO user
) {
}
