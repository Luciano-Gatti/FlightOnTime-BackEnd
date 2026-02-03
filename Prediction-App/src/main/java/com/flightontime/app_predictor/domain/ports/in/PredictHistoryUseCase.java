package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.PredictHistoryDetail;
import com.flightontime.app_predictor.domain.model.PredictHistoryItem;
import java.util.List;

/**
 * Interfaz PredictHistoryUseCase.
 */
public interface PredictHistoryUseCase {
    List<PredictHistoryItem> getHistory(Long userId);

    PredictHistoryDetail getHistoryDetail(Long userId, Long requestId);
}
