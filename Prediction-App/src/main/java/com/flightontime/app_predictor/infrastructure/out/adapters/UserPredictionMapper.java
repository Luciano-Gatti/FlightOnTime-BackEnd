package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserPredictionMapper {
    public UserPrediction toDomain(UserPredictionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserPrediction(
                entity.getId(),
                entity.getUserId(),
                entity.getPredictionId(),
                entity.getCreatedAt()
        );
    }

    public UserPredictionEntity toEntity(UserPrediction userPrediction, UserPredictionEntity entity) {
        if (userPrediction == null) {
            return entity;
        }
        UserPredictionEntity target = entity == null ? new UserPredictionEntity() : entity;
        target.setUserId(userPrediction.userId());
        target.setPredictionId(userPrediction.predictionId());
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
