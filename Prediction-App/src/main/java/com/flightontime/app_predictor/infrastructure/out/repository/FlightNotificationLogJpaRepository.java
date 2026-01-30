package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightNotificationLogEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightNotificationLogJpaRepository extends JpaRepository<FlightNotificationLogEntity, Long> {
    Optional<FlightNotificationLogEntity> findFirstByUserIdAndRequestIdAndType(Long userId, Long requestId, String type);
}
