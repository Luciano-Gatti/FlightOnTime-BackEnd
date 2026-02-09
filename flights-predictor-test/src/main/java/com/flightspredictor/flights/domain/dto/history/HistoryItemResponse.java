package com.flightspredictor.flights.domain.dto.history;

import com.flightspredictor.flights.domain.enums.PredictedStatus;
import java.time.LocalDateTime;

public record HistoryItemResponse(
        Long snapshotId,
        Long predictionId,
        LocalDateTime flightDateUtc,
        String origin,
        String dest,
        PredictedStatus predictedStatus,
        Double predictedProbability,
        String confidence,
        LocalDateTime createdAt
) {
}
