package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.StatsSummary;

/**
 * Interfaz StatsSummaryUseCase.
 */
public interface StatsSummaryUseCase {
    StatsSummary getSummary(int topN);
}
