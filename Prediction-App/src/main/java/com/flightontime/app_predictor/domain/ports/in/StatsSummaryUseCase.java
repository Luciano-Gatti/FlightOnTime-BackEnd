package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.StatsSummaryResponseDTO;

/**
 * Interfaz StatsSummaryUseCase.
 */
public interface StatsSummaryUseCase {
    StatsSummaryResponseDTO getSummary(int topN);
}
