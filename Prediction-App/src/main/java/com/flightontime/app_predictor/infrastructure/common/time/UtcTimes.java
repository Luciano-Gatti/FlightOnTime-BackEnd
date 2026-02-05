package com.flightontime.app_predictor.infrastructure.common.time;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Utilidad para normalizar fechas en UTC.
 */
public final class UtcTimes {

    private UtcTimes() {
    }

    /**
     * Convierte un valor OffsetDateTime a UTC preservando el instante.
     * @param value variable de entrada value.
     * @return resultado de la operación to utc.
     */
    public static OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
