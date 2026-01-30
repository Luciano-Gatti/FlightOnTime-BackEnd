package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;

public interface StatsAccuracyByLeadTimeUseCase {
    StatsAccuracyByLeadTimeResponseDTO getAccuracyByLeadTime();
}
