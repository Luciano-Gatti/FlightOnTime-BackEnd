package com.flightontime.app_predictor.infrastructure.out.repository;

import java.time.OffsetDateTime;

public interface PredictionAccuracyView {
    OffsetDateTime getFlightDateUtc();

    OffsetDateTime getForecastBucketUtc();

    String getPredictedStatus();

    String getActualStatus();
}
