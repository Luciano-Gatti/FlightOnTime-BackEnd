package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionSnapshotEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserPredictionMapper {
    public UserPrediction toDomain(UserPredictionSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserPrediction(
                entity.getId(),
                entity.getUserId(),
                entity.getFlightPredictionId(),
                entity.getCreatedAt()
        );
    }

    public UserPredictionSnapshotEntity toEntity(UserPrediction userPrediction, UserPredictionSnapshotEntity entity) {
        if (userPrediction == null) {
            return entity;
        }
        UserPredictionSnapshotEntity target = entity == null ? new UserPredictionSnapshotEntity() : entity;
        target.setUserId(userPrediction.userId());
        target.setFlightPredictionId(userPrediction.flightPredictionId());
        if (userPrediction.createdAt() != null) {
            target.setCreatedAt(toUtc(userPrediction.createdAt()));
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
