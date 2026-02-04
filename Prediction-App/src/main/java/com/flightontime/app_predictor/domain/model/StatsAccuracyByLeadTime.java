package com.flightontime.app_predictor.domain.model;

import java.util.List;

/**
 * Registro StatsAccuracyByLeadTime.
 * @param bins variable de entrada bins.
 * @return resultado de la operación resultado.
 */
public record StatsAccuracyByLeadTime(
        List<StatsAccuracyBin> bins
) {
}
