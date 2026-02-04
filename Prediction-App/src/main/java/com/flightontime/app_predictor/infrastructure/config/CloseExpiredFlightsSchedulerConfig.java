package com.flightontime.app_predictor.infrastructure.config;

import com.flightontime.app_predictor.application.services.CloseExpiredFlightsJobService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Clase CloseExpiredFlightsSchedulerConfig.
 */
@Configuration
@EnableScheduling
public class CloseExpiredFlightsSchedulerConfig {
    private final CloseExpiredFlightsJobService closeExpiredFlightsJobService;

    /**
     * Ejecuta la operación close expired flights scheduler config.
     * @param closeExpiredFlightsJobService variable de entrada closeExpiredFlightsJobService.
     */

    /**
     * Ejecuta la operación close expired flights scheduler config.
     * @param closeExpiredFlightsJobService variable de entrada closeExpiredFlightsJobService.
     * @return resultado de la operación close expired flights scheduler config.
     */

    public CloseExpiredFlightsSchedulerConfig(CloseExpiredFlightsJobService closeExpiredFlightsJobService) {
        this.closeExpiredFlightsJobService = closeExpiredFlightsJobService;
    }

    /**
     * Ejecuta la operación close expired flights.
     */
    @Scheduled(cron = "0 10 0 * * *", zone = "America/Argentina/Buenos_Aires")
    public void closeExpiredFlights() {
        closeExpiredFlightsJobService.closeExpiredFlights();
    }
}
