package com.flightontime.app_predictor.infrastructure.in.dto;

import java.util.List;

/**
 * Registro StatsAccuracyByLeadTimeResponseDTO.
 * @param bins variable de entrada bins.
 * @return resultado de la operación resultado.
 */
public record StatsAccuracyByLeadTimeResponseDTO(
        List<StatsAccuracyBinDTO> bins
) {
}
