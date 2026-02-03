package com.flightontime.app_predictor.domain.model;

import java.util.List;

/**
 * Registro StatsSummary.
 */
public record StatsSummary(
        long totalPredictions,
        long totalOnTimePredicted,
        long totalDelayedPredicted,
        long totalFlightsWithActual,
        long totalCancelled,
        List<StatsTopFlight> topFlights
) {
}
