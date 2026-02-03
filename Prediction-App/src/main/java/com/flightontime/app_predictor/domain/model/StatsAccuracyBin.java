package com.flightontime.app_predictor.domain.model;

/**
 * Registro StatsAccuracyBin.
 */
public record StatsAccuracyBin(
        String leadTimeHours,
        long total,
        long correct,
        double accuracy
) {
}
