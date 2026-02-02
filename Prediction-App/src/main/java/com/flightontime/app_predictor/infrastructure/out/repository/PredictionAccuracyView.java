package com.flightontime.app_predictor.infrastructure.out.repository;

import java.time.OffsetDateTime;

public interface PredictionAccuracyView {
    OffsetDateTime getFlightDateUtc();

    OffsetDateTime getPredictedAt();

    String getPredictedStatus();

    String getActualStatus();
}
