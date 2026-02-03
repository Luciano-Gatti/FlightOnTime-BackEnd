package com.flightontime.app_predictor.infrastructure.in.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Object details
) {
}
