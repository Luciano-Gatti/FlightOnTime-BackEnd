package com.flightspredictor.flights.domain.dto.prediction;

import com.flightspredictor.flights.domain.enum.Prevision;
import com.flightspredictor.flights.domain.enum.Status;
import java.util.Map;

public record PredictionStatsResponse(
        long totalPredictions,
        Map<Status, Long> byStatus,
        Map<Prevision, Long> byPrevision,
        Double averageProbability
) {
}
