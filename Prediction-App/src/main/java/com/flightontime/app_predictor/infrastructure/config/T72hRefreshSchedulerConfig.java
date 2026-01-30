package com.flightontime.app_predictor.infrastructure.config;

import com.flightontime.app_predictor.application.services.T72hRefreshJobService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class T72hRefreshSchedulerConfig {
    private final T72hRefreshJobService refreshJobService;

    public T72hRefreshSchedulerConfig(T72hRefreshJobService refreshJobService) {
        this.refreshJobService = refreshJobService;
    }

    @Scheduled(fixedRateString = "PT3H")
    public void refreshPredictions() {
        refreshJobService.refreshPredictions();
    }
}
