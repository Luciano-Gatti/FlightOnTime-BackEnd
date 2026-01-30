package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

public record NotificationLog(
        Long id,
        Long userPredictionId,
        String channel,
        String status,
        String message,
        OffsetDateTime sentAt,
        OffsetDateTime createdAt
) {
}
