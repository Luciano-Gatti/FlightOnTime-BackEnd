package com.flightontime.app_predictor.infrastructure.config;

import com.flightontime.app_predictor.application.services.ActualsFetchJobService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Clase ActualsFetchSchedulerConfig.
 */
@Configuration
@EnableScheduling
public class ActualsFetchSchedulerConfig {
    private final ActualsFetchJobService actualsFetchJobService;

    /**
     * Ejecuta la operación actuals fetch scheduler config.
     * @param actualsFetchJobService variable de entrada actualsFetchJobService.
     */

    /**
     * Ejecuta la operación actuals fetch scheduler config.
     * @param actualsFetchJobService variable de entrada actualsFetchJobService.
     * @return resultado de la operación actuals fetch scheduler config.
     */

    public ActualsFetchSchedulerConfig(ActualsFetchJobService actualsFetchJobService) {
        this.actualsFetchJobService = actualsFetchJobService;
    }

    /**
     * Ejecuta la operación fetch actuals.
     */
    @Scheduled(cron = "0 59 23 * * *", zone = "America/Argentina/Buenos_Aires")
    public void fetchActuals() {
        actualsFetchJobService.fetchActuals();
    }
}
