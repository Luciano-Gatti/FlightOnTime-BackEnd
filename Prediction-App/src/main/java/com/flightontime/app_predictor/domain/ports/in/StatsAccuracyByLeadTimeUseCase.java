package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.StatsAccuracyByLeadTime;

/**
 * Interfaz StatsAccuracyByLeadTimeUseCase.
 */
public interface StatsAccuracyByLeadTimeUseCase {
    StatsAccuracyByLeadTime getAccuracyByLeadTime();
}
