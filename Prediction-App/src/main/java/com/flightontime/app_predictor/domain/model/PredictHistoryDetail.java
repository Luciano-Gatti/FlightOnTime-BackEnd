package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Registro PredictHistoryDetail.
 */
public record PredictHistoryDetail(
        Long flightRequestId,
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        long uniqueUsersCount,
        List<PredictHistoryPrediction> predictions
) {
}
