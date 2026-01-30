package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.infrastructure.out.entities.NotificationLogEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class NotificationLogMapper {
    public NotificationLog toDomain(NotificationLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new NotificationLog(
                entity.getId(),
                entity.getUserPredictionId(),
                entity.getChannel(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getSentAt(),
                entity.getCreatedAt()
        );
    }

    public NotificationLogEntity toEntity(NotificationLog notificationLog, NotificationLogEntity entity) {
        if (notificationLog == null) {
            return entity;
        }
        NotificationLogEntity target = entity == null ? new NotificationLogEntity() : entity;
        target.setUserPredictionId(notificationLog.userPredictionId());
        target.setChannel(notificationLog.channel());
        target.setStatus(notificationLog.status());
        target.setMessage(notificationLog.message());
        target.setSentAt(toUtc(notificationLog.sentAt()));
        if (notificationLog.createdAt() != null) {
            target.setCreatedAt(toUtc(notificationLog.createdAt()));
        }
        return target;
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
