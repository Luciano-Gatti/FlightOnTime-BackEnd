package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightSubscriptionEntity;

/**
 * Clase FlightFollowMapper.
 */
public class FlightFollowMapper {
    public FlightFollow toDomain(FlightSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlightFollow(
                entity.getId(),
                entity.getUserId(),
                entity.getFlightRequestId(),
                entity.getRefreshMode(),
                entity.getBaselineSnapshotId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public FlightSubscriptionEntity toEntity(FlightFollow flightFollow, FlightSubscriptionEntity entity) {
        FlightSubscriptionEntity target = entity == null ? new FlightSubscriptionEntity() : entity;
        target.setUserId(flightFollow.userId());
        target.setFlightRequestId(flightFollow.flightRequestId());
        target.setRefreshMode(flightFollow.refreshMode());
        target.setBaselineSnapshotId(flightFollow.baselineSnapshotId());
        target.setCreatedAt(flightFollow.createdAt());
        target.setUpdatedAt(flightFollow.updatedAt());
        return target;
    }
}
