package com.flightspredictor.flights.domain.dto.prediction;

import com.flightspredictor.flights.domain.enums.Prevision;
import com.flightspredictor.flights.domain.enums.Status;
import java.util.Map;

public record PredictionStatsResponse(
        long totalPredictions,
        Map<Status, Long> byStatus,
        Map<Prevision, Long> byPrevision,
        Double averageProbability
) {
}
