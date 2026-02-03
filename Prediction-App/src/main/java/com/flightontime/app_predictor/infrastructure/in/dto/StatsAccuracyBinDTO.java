package com.flightontime.app_predictor.infrastructure.in.dto;

/**
 * Registro StatsAccuracyBinDTO.
 */
public record StatsAccuracyBinDTO(
        String leadTimeHours,
        long total,
        long correct,
        double accuracy
) {
}
