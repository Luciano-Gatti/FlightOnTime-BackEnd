package com.flightontime.app_predictor.domain.ports.in;

import com.flightontime.app_predictor.application.dto.BulkPredictResult;
import java.io.InputStream;

public interface BulkPredictUseCase {
    BulkPredictResult importPredictionsFromCsv(InputStream inputStream, Long userId, boolean dryRun);
}
