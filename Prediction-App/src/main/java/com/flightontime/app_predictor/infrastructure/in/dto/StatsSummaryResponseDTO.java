package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

/**
 * Registro StatsSummaryResponseDTO.
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
