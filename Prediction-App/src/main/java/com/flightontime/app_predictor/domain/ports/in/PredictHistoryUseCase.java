package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryDetailDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryItemDTO;
import java.util.List;

/**
 * Interfaz PredictHistoryUseCase.
 */
public interface PredictHistoryUseCase {
    List<PredictHistoryItemDTO> getHistory(Long userId);

    PredictHistoryDetailDTO getHistoryDetail(Long userId, Long requestId);
}
