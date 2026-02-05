package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightNotificationLogEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz FlightNotificationLogJpaRepository.
 */
public interface FlightNotificationLogJpaRepository extends JpaRepository<FlightNotificationLogEntity, Long> {
    /**
     * Ejecuta la operación find first by user id and request id and type.
     * @param userId variable de entrada userId.
     * @param requestId variable de entrada requestId.
     * @param type variable de entrada type.
     * @return resultado de la operación find first by user id and request id and type.
     */
    Optional<FlightNotificationLogEntity> findFirstByUserIdAndRequestIdAndType(Long userId, Long requestId, String type);
}
