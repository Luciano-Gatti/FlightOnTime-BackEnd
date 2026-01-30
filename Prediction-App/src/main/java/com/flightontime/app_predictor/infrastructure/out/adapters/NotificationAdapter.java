package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapter implements NotificationPort {
    private static final Logger logger = LoggerFactory.getLogger(NotificationAdapter.class);

    @Override
    public void sendT12hStatusChange(
            Long userId,
            FlightRequest request,
            Prediction baseline,
            Prediction current
    ) {
        logger.info(
                "T12h notification for user {} request {} status {} -> {}",
                userId,
                request.id(),
                baseline.status(),
                current.status()
        );
    }
}
