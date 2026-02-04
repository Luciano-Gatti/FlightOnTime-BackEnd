package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

/**
 * Registro StatsSummaryResponseDTO.
 * @param totalPredictions variable de entrada totalPredictions.
 * @param totalOnTimePredicted variable de entrada totalOnTimePredicted.
 * @param totalDelayedPredicted variable de entrada totalDelayedPredicted.
 * @param totalFlightsWithActual variable de entrada totalFlightsWithActual.
 * @param totalCancelled variable de entrada totalCancelled.
 * @param topFlights variable de entrada topFlights.
 * @return resultado de la operación resultado.
 */
public record StatsSummaryResponseDTO(
        long totalPredictions,
        long totalOnTimePredicted,
        long totalDelayedPredicted,
        long totalFlightsWithActual,
        long totalCancelled,
        List<StatsTopFlightDTO> topFlights
) {
}
