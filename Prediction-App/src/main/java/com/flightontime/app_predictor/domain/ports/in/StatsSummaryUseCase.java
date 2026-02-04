package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.StatsSummary;

/**
 * Interfaz StatsSummaryUseCase.
 */
public interface StatsSummaryUseCase {
    /**
     * Ejecuta la operación get summary.
     * @param topN variable de entrada topN.
     * @return resultado de la operación get summary.
     */
    StatsSummary getSummary(int topN);
}
