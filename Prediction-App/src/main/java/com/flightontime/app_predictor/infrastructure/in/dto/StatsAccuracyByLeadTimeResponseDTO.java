package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

/**
 * Registro StatsAccuracyByLeadTimeResponseDTO.
 */
public record StatsAccuracyByLeadTimeResponseDTO(
        List<StatsAccuracyBinDTO> bins
) {
}
