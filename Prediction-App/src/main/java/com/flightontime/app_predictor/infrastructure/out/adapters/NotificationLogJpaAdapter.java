package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.NotificationLogEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.NotificationLogJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogJpaAdapter implements NotificationLogRepositoryPort {
    private final NotificationLogJpaRepository notificationLogJpaRepository;
    private final NotificationLogMapper notificationLogMapper = new NotificationLogMapper();

    public NotificationLogJpaAdapter(NotificationLogJpaRepository notificationLogJpaRepository) {
        this.notificationLogJpaRepository = notificationLogJpaRepository;
    }

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        if (notificationLog == null) {
            throw new IllegalArgumentException("Notification log is required");
        }
        NotificationLogEntity entity = resolveEntity(notificationLog.id());
        notificationLogMapper.toEntity(notificationLog, entity);
        return notificationLogMapper.toDomain(notificationLogJpaRepository.save(entity));
    }

    @Override
    public Optional<NotificationLog> findById(Long id) {
        return notificationLogJpaRepository.findById(id)
                .map(notificationLogMapper::toDomain);
    }

    private NotificationLogEntity resolveEntity(Long id) {
        if (id == null) {
            return new NotificationLogEntity();
        }
        return notificationLogJpaRepository.findById(id).orElseGet(NotificationLogEntity::new);
    }
}
