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

    public CloseExpiredFlightsSchedulerConfig(CloseExpiredFlightsJobService closeExpiredFlightsJobService) {
        this.closeExpiredFlightsJobService = closeExpiredFlightsJobService;
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "America/Argentina/Buenos_Aires")
    public void closeExpiredFlights() {
        closeExpiredFlightsJobService.closeExpiredFlights();
    }
}
