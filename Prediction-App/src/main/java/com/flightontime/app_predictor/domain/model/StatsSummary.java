package com.flightontime.app_predictor.domain.model;

import java.util.List;

/**
 * Registro StatsSummary.
 * @param totalPredictions variable de entrada totalPredictions.
 * @param totalOnTimePredicted variable de entrada totalOnTimePredicted.
 * @param totalDelayedPredicted variable de entrada totalDelayedPredicted.
 * @param totalFlightsWithActual variable de entrada totalFlightsWithActual.
 * @param totalCancelled variable de entrada totalCancelled.
 * @param topFlights variable de entrada topFlights.
 * @return resultado de la operación resultado.
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
