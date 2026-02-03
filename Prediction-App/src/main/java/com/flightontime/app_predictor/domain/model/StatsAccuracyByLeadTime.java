package com.flightontime.app_predictor.domain.model;

import java.util.List;

/**
 * Registro StatsAccuracyByLeadTime.
 */
public record StatsAccuracyByLeadTime(
        List<StatsAccuracyBin> bins
) {
}
