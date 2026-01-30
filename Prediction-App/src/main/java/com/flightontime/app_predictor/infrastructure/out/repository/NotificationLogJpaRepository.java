package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogEntity, Long> {
}
