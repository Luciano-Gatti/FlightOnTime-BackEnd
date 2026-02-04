package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.StatsAccuracyByLeadTime;

/**
 * Interfaz StatsAccuracyByLeadTimeUseCase.
 */
public interface StatsAccuracyByLeadTimeUseCase {
    /**
     * Ejecuta la operación get accuracy by lead time.
     * @return resultado de la operación get accuracy by lead time.
     */
    StatsAccuracyByLeadTime getAccuracyByLeadTime();
}
