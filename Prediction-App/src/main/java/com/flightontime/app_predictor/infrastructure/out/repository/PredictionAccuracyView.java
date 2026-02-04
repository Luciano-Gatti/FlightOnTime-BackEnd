package com.flightontime.app_predictor.infrastructure.out.repository;

import java.time.OffsetDateTime;

/**
 * Interfaz PredictionAccuracyView.
 */
public interface PredictionAccuracyView {
    /**
     * Ejecuta la operación get flight date utc.
     * @return resultado de la operación get flight date utc.
     */
    OffsetDateTime getFlightDateUtc();

    /**
     * Ejecuta la operación get forecast bucket utc.
     * @return resultado de la operación get forecast bucket utc.
     */

    OffsetDateTime getForecastBucketUtc();

    /**
     * Ejecuta la operación get predicted status.
     * @return resultado de la operación get predicted status.
     */

    String getPredictedStatus();

    /**
     * Ejecuta la operación get actual status.
     * @return resultado de la operación get actual status.
     */

    String getActualStatus();
}
