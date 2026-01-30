package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightFollowEntity;

public class FlightFollowMapper {
    public FlightFollow toDomain(FlightFollowEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlightFollow(
                entity.getId(),
                entity.getUserId(),
                entity.getRequestId(),
                entity.getRefreshMode(),
                entity.getBaselinePredictionId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public FlightFollowEntity toEntity(FlightFollow flightFollow, FlightFollowEntity entity) {
        FlightFollowEntity target = entity == null ? new FlightFollowEntity() : entity;
        target.setUserId(flightFollow.userId());
        target.setRequestId(flightFollow.requestId());
        target.setRefreshMode(flightFollow.refreshMode());
        target.setBaselinePredictionId(flightFollow.baselinePredictionId());
        target.setCreatedAt(flightFollow.createdAt());
        target.setUpdatedAt(flightFollow.updatedAt());
        return target;
    }
}
