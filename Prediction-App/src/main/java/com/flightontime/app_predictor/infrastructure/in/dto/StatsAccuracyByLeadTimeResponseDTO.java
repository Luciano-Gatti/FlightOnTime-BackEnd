package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

public record StatsAccuracyByLeadTimeResponseDTO(
        List<StatsAccuracyBinDTO> bins
) {
}
