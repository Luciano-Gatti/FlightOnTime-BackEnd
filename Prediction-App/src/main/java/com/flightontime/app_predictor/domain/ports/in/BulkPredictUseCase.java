package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.application.dto.BulkPredictResult;
import java.io.InputStream;

/**
 * Interfaz BulkPredictUseCase.
 */
public interface BulkPredictUseCase {
    BulkPredictResult importPredictionsFromCsv(InputStream inputStream, Long userId, boolean dryRun);
}
