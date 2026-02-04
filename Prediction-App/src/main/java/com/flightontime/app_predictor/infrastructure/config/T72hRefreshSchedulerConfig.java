package com.flightontime.app_predictor.infrastructure.config;

import com.flightontime.app_predictor.application.services.T72hRefreshJobService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Clase T72hRefreshSchedulerConfig.
 */
@Configuration
@EnableScheduling
public class T72hRefreshSchedulerConfig {
    private final T72hRefreshJobService refreshJobService;

    /**
     * Ejecuta la operación t72h refresh scheduler config.
     * @param refreshJobService variable de entrada refreshJobService.
     */

    /**
     * Ejecuta la operación t72h refresh scheduler config.
     * @param refreshJobService variable de entrada refreshJobService.
     * @return resultado de la operación t72h refresh scheduler config.
     */

    public T72hRefreshSchedulerConfig(T72hRefreshJobService refreshJobService) {
        this.refreshJobService = refreshJobService;
    }

    /**
     * Ejecuta la operación refresh predictions.
     */
    @Scheduled(fixedRateString = "PT3H")
    public void refreshPredictions() {
        refreshJobService.refreshPredictions();
    }
}
