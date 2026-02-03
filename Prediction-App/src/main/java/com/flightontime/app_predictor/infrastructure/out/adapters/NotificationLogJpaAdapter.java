package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightNotificationLogEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightNotificationLogJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Clase NotificationLogJpaAdapter.
 */
@Component
public class NotificationLogJpaAdapter implements NotificationLogRepositoryPort {
    private final FlightNotificationLogJpaRepository flightNotificationLogJpaRepository;
    private final NotificationLogMapper notificationLogMapper = new NotificationLogMapper();

    public NotificationLogJpaAdapter(FlightNotificationLogJpaRepository flightNotificationLogJpaRepository) {
        this.flightNotificationLogJpaRepository = flightNotificationLogJpaRepository;
    }

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        if (notificationLog == null) {
            throw new IllegalArgumentException("Notification log is required");
        }
        FlightNotificationLogEntity entity = resolveEntity(notificationLog.id());
        notificationLogMapper.toEntity(notificationLog, entity);
        return notificationLogMapper.toDomain(flightNotificationLogJpaRepository.save(entity));
    }

    @Override
    public Optional<NotificationLog> findById(Long id) {
        return flightNotificationLogJpaRepository.findById(id)
                .map(notificationLogMapper::toDomain);
    }

    @Override
    public Optional<NotificationLog> findByUserIdAndRequestIdAndType(Long userId, Long requestId, String type) {
        return flightNotificationLogJpaRepository.findFirstByUserIdAndRequestIdAndType(userId, requestId, type)
                .map(notificationLogMapper::toDomain);
    }

    private FlightNotificationLogEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightNotificationLogEntity();
        }
        return flightNotificationLogJpaRepository.findById(id).orElseGet(FlightNotificationLogEntity::new);
    }
}
