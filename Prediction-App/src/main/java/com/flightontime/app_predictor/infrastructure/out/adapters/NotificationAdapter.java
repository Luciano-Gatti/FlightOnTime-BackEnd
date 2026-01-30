package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapter implements NotificationPort {
    private static final Logger logger = LoggerFactory.getLogger(NotificationAdapter.class);

    @Override
    public void sendT12hSummary(Long userId, List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        logger.info("T12h notification summary for user {}: {}", userId, String.join(" | ", messages));
    }
}
