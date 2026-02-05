package com.flightontime.app_predictor.infrastructure.out.mapper;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.UserPredictionSnapshotEntity;
import static com.flightontime.app_predictor.infrastructure.common.time.UtcTimes.toUtc;

/**
 * Clase UserPredictionMapper.
 */
public class UserPredictionMapper {
    /**
     * Ejecuta la operación to domain.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to domain.
     */
    public UserPrediction toDomain(UserPredictionSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserPrediction(
                entity.getId(),
                entity.getUserId(),
                entity.getFlightRequestId(),
                entity.getFlightPredictionId(),
                entity.getSource(),
                entity.getCreatedAt()
        );
    }

    /**
     * Ejecuta la operación to entity.
     * @param userPrediction variable de entrada userPrediction.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to entity.
     */

    public UserPredictionSnapshotEntity toEntity(UserPrediction userPrediction, UserPredictionSnapshotEntity entity) {
        if (userPrediction == null) {
            return entity;
        }
        UserPredictionSnapshotEntity target = entity == null ? new UserPredictionSnapshotEntity() : entity;
        target.setUserId(userPrediction.userId());
        target.setFlightRequestId(userPrediction.flightRequestId());
        target.setFlightPredictionId(userPrediction.flightPredictionId());
        target.setSource(userPrediction.source());
        if (userPrediction.createdAt() != null) {
            target.setCreatedAt(toUtc(userPrediction.createdAt()));
        }
        return target;
    }

}
