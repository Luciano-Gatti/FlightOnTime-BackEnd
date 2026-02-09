package com.flightspredictor.flights.infra.security;

import java.time.LocalDateTime;

public record SecurityErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        LocalDateTime timestamp
) {
    public static SecurityErrorResponse unauthorized(
            int status,
            String error,
            String message,
            String path,
            String correlationId
    ) {
        return new SecurityErrorResponse(
                status,
                error,
                message,
                path,
                correlationId,
                LocalDateTime.now()
        );
    }
}
