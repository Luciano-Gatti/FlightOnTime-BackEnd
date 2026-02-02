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
                entity.getFlightDateUtc(),
                entity.getAirlineCode(),
                entity.getOriginIata(),
                entity.getDestIata(),
                entity.getFlightNumber(),
                entity.getCreatedAt(),
                entity.getActive() == null || entity.getActive(),
                entity.getClosedAt()
        );
    }

    public FlightRequestEntity toEntity(FlightRequest request, FlightRequestEntity entity) {
        if (request == null) {
            return entity;
        }
        FlightRequestEntity target = entity == null ? new FlightRequestEntity() : entity;
        target.setUserId(request.userId());
        target.setFlightDateUtc(toUtc(request.flightDateUtc()));
        target.setAirlineCode(request.airlineCode());
        target.setOriginIata(request.originIata());
        target.setDestIata(request.destIata());
        target.setFlightNumber(request.flightNumber());
        target.setActive(request.active());
        target.setClosedAt(toUtc(request.closedAt()));
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
