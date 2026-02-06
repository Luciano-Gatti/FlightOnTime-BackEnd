package com.flightspredictor.flights.domain.dto.prediction;

import com.flightspredictor.flights.domain.enums.PredictedStatus;
import java.util.Map;

public record PredictionStatsResponse(
        long totalPredictions,
        Map<PredictedStatus, Long> byStatus,
        Map<String, Long> byConfidence,
        Double averageProbability
) {
}
