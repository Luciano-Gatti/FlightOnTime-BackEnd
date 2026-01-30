package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import java.util.Optional;

public interface NotificationLogRepositoryPort {
    NotificationLog save(NotificationLog notificationLog);

    Optional<NotificationLog> findById(Long id);

    Optional<NotificationLog> findByUserIdAndRequestIdAndType(Long userId, Long requestId, String type);
}
