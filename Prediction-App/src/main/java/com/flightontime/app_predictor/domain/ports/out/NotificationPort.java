package com.flightontime.app_predictor.domain.ports.out;

import java.util.List;

public interface NotificationPort {
    void sendT12hSummary(Long userId, List<String> messages);
}
