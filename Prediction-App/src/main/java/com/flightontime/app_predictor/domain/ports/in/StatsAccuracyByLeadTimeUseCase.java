package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;

/**
 * Interfaz StatsAccuracyByLeadTimeUseCase.
 */
public interface StatsAccuracyByLeadTimeUseCase {
    StatsAccuracyByLeadTimeResponseDTO getAccuracyByLeadTime();
}
