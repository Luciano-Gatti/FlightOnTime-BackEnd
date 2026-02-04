package com.flightontime.app_predictor.domain.model;

/**
 * Registro StatsAccuracyBin.
 * @param leadTimeHours variable de entrada leadTimeHours.
 * @param total variable de entrada total.
 * @param correct variable de entrada correct.
 * @param accuracy variable de entrada accuracy.
 * @return resultado de la operación resultado.
 */
public record StatsAccuracyBin(
        String leadTimeHours,
        long total,
        long correct,
        double accuracy
) {
}
