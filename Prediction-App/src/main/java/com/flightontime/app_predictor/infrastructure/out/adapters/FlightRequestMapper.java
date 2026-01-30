package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class FlightRequestMapper {
    public FlightRequest toDomain(FlightRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlightRequest(
                entity.getId(),
                entity.getUserId(),
                entity.getFlightDate(),
                entity.getCarrier(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getFlightNumber(),
                entity.getCreatedAt()
        );
    }

    public FlightRequestEntity toEntity(FlightRequest request, FlightRequestEntity entity) {
        if (request == null) {
            return entity;
        }
        FlightRequestEntity target = entity == null ? new FlightRequestEntity() : entity;
        target.setUserId(request.userId());
        target.setFlightDate(toUtc(request.flightDate()));
        target.setCarrier(request.carrier());
        target.setOrigin(request.origin());
        target.setDestination(request.destination());
        target.setFlightNumber(request.flightNumber());
        if (request.createdAt() != null) {
            target.setCreatedAt(toUtc(request.createdAt()));
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
