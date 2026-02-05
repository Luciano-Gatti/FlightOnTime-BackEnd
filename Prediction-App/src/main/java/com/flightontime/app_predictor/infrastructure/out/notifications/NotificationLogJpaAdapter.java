package com.flightontime.app_predictor.infrastructure.out.notifications;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.mapper.NotificationLogMapper;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightNotificationLogEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightNotificationLogJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Clase NotificationLogJpaAdapter.
 */
@Component
public class NotificationLogJpaAdapter implements NotificationLogRepositoryPort {
    private final FlightNotificationLogJpaRepository flightNotificationLogJpaRepository;
    private final NotificationLogMapper notificationLogMapper = new NotificationLogMapper();

    /**
     * Ejecuta la operación notification log jpa adapter.
     * @param flightNotificationLogJpaRepository variable de entrada flightNotificationLogJpaRepository.
     */

    /**
     * Ejecuta la operación notification log jpa adapter.
     * @param flightNotificationLogJpaRepository variable de entrada flightNotificationLogJpaRepository.
     * @return resultado de la operación notification log jpa adapter.
     */

    public NotificationLogJpaAdapter(FlightNotificationLogJpaRepository flightNotificationLogJpaRepository) {
        this.flightNotificationLogJpaRepository = flightNotificationLogJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param notificationLog variable de entrada notificationLog.
     * @return resultado de la operación save.
     */
    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        if (notificationLog == null) {
            throw new IllegalArgumentException("Notification log is required");
        }
        FlightNotificationLogEntity entity = resolveEntity(notificationLog.id());
        notificationLogMapper.toEntity(notificationLog, entity);
        return notificationLogMapper.toDomain(flightNotificationLogJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */
    @Override
    public Optional<NotificationLog> findById(Long id) {
        return flightNotificationLogJpaRepository.findById(id)
                .map(notificationLogMapper::toDomain);
    }

    /**
     * Ejecuta la operación find by user id and request id and type.
     * @param userId variable de entrada userId.
     * @param requestId variable de entrada requestId.
     * @param type variable de entrada type.
     * @return resultado de la operación find by user id and request id and type.
     */
    @Override
    public Optional<NotificationLog> findByUserIdAndRequestIdAndType(Long userId, Long requestId, String type) {
        return flightNotificationLogJpaRepository.findFirstByUserIdAndRequestIdAndType(userId, requestId, type)
                .map(notificationLogMapper::toDomain);
    }

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private FlightNotificationLogEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightNotificationLogEntity();
        }
        return flightNotificationLogJpaRepository.findById(id).orElseGet(FlightNotificationLogEntity::new);
    }
}
