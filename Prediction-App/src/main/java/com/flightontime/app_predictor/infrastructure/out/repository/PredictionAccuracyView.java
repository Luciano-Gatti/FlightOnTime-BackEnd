package com.flightontime.app_predictor.infrastructure.out.repository;

import java.time.OffsetDateTime;

public interface PredictionAccuracyView {
    OffsetDateTime getFlightDate();

    OffsetDateTime getPredictedAt();

    String getPredictedStatus();

    String getActualStatus();
}
