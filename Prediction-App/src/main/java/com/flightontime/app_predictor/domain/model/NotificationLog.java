package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro NotificationLog.
 * @param id variable de entrada id.
 * @param userId variable de entrada userId.
 * @param requestId variable de entrada requestId.
 * @param type variable de entrada type.
 * @param userPredictionId variable de entrada userPredictionId.
 * @param channel variable de entrada channel.
 * @param status variable de entrada status.
 * @param message variable de entrada message.
 * @param sentAt variable de entrada sentAt.
 * @param createdAt variable de entrada createdAt.
 * @return resultado de la operación resultado.
 */
public record NotificationLog(
        Long id,
        Long userId,
        Long requestId,
        String type,
        Long userPredictionId,
        String channel,
        String status,
        String message,
        OffsetDateTime sentAt,
        OffsetDateTime createdAt
) {
}
