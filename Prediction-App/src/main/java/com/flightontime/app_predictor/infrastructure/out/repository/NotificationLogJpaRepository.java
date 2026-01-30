package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.NotificationLogEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogEntity, Long> {
    Optional<NotificationLogEntity> findFirstByUserIdAndRequestIdAndType(Long userId, Long requestId, String type);
}
