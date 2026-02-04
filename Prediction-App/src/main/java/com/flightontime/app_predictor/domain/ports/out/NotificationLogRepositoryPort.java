package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import java.util.Optional;

/**
 * Interfaz NotificationLogRepositoryPort.
 */
public interface NotificationLogRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param notificationLog variable de entrada notificationLog.
     * @return resultado de la operación save.
     */
    NotificationLog save(NotificationLog notificationLog);

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */

    Optional<NotificationLog> findById(Long id);

    /**
     * Ejecuta la operación find by user id and request id and type.
     * @param userId variable de entrada userId.
     * @param requestId variable de entrada requestId.
     * @param type variable de entrada type.
     * @return resultado de la operación find by user id and request id and type.
     */

    Optional<NotificationLog> findByUserIdAndRequestIdAndType(Long userId, Long requestId, String type);
}
