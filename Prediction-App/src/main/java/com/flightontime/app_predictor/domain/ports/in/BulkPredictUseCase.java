package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.application.dto.BulkPredictResult;
import java.io.InputStream;

/**
 * Interfaz BulkPredictUseCase.
 */
public interface BulkPredictUseCase {
    /**
     * Ejecuta la operación import predictions from csv.
     * @param inputStream variable de entrada inputStream.
     * @param userId variable de entrada userId.
     * @param dryRun variable de entrada dryRun.
     * @return resultado de la operación import predictions from csv.
     */
    BulkPredictResult importPredictionsFromCsv(InputStream inputStream, Long userId, boolean dryRun);
}
