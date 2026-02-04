package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro UserPrediction.
 * @param id variable de entrada id.
 * @param userId variable de entrada userId.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param flightPredictionId variable de entrada flightPredictionId.
 * @param source variable de entrada source.
 * @param createdAt variable de entrada createdAt.
 * @return resultado de la operación resultado.
 */
public record UserPrediction(
        Long id,
        Long userId,
        Long flightRequestId,
        Long flightPredictionId,
        UserPredictionSource source,
        OffsetDateTime createdAt
) {
}
