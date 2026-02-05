package com.flightontime.app_predictor.infrastructure.out.mapper;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightNotificationLogEntity;
import static com.flightontime.app_predictor.infrastructure.common.time.UtcTimes.toUtc;

/**
 * Clase NotificationLogMapper.
 */
public class NotificationLogMapper {
    /**
     * Ejecuta la operación to domain.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to domain.
     */
    public NotificationLog toDomain(FlightNotificationLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new NotificationLog(
                entity.getId(),
                entity.getUserId(),
                entity.getRequestId(),
                entity.getType(),
                entity.getUserPredictionId(),
                entity.getChannel(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getSentAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Ejecuta la operación to entity.
     * @param notificationLog variable de entrada notificationLog.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to entity.
     */

    public FlightNotificationLogEntity toEntity(NotificationLog notificationLog, FlightNotificationLogEntity entity) {
        if (notificationLog == null) {
            return entity;
        }
        FlightNotificationLogEntity target = entity == null ? new FlightNotificationLogEntity() : entity;
        target.setUserPredictionId(notificationLog.userPredictionId());
        target.setUserId(notificationLog.userId());
        target.setRequestId(notificationLog.requestId());
        target.setType(notificationLog.type());
        target.setChannel(notificationLog.channel());
        target.setStatus(notificationLog.status());
        target.setMessage(notificationLog.message());
        target.setSentAt(toUtc(notificationLog.sentAt()));
        if (notificationLog.createdAt() != null) {
            target.setCreatedAt(toUtc(notificationLog.createdAt()));
        }
        return target;
    }

}
