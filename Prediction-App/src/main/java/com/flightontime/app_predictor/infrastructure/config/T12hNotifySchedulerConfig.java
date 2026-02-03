package com.flightontime.app_predictor.infrastructure.config;

import com.flightontime.app_predictor.application.services.T12hNotifyJobService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Clase T12hNotifySchedulerConfig.
 */
@Configuration
@EnableScheduling
public class T12hNotifySchedulerConfig {
    private final T12hNotifyJobService notifyJobService;

    public T12hNotifySchedulerConfig(T12hNotifyJobService notifyJobService) {
        this.notifyJobService = notifyJobService;
    }

    @Scheduled(fixedRateString = "PT1H")
    public void notifyUsers() {
        notifyJobService.notifyUsers();
    }
}
