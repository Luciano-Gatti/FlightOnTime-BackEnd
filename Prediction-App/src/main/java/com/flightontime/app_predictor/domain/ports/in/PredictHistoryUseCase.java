package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.domain.model.PredictHistoryDetail;
import com.flightontime.app_predictor.domain.model.PredictHistoryItem;
import java.util.List;

/**
 * Interfaz PredictHistoryUseCase.
 */
public interface PredictHistoryUseCase {
    /**
     * Ejecuta la operación get history.
     * @param userId variable de entrada userId.
     * @return resultado de la operación get history.
     */
    List<PredictHistoryItem> getHistory(Long userId);

    /**
     * Ejecuta la operación get history detail.
     * @param userId variable de entrada userId.
     * @param requestId variable de entrada requestId.
     * @return resultado de la operación get history detail.
     */

    PredictHistoryDetail getHistoryDetail(Long userId, Long requestId);
}
