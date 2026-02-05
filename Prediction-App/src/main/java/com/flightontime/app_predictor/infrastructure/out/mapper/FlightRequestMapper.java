package com.flightontime.app_predictor.infrastructure.out.mapper;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightRequestEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Clase FlightRequestMapper.
 */
public class FlightRequestMapper {
    /**
     * Ejecuta la operación to domain.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to domain.
     */
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
                entity.getDistance() == null ? 0.0 : entity.getDistance(),
                entity.getFlightNumber(),
                entity.getCreatedAt(),
                entity.getActive() == null || entity.getActive(),
                entity.getClosedAt()
        );
    }

    /**
     * Ejecuta la operación to entity.
     * @param request variable de entrada request.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to entity.
     */

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
        target.setDistance(request.distance());
        target.setFlightNumber(request.flightNumber());
        target.setActive(request.active());
        target.setClosedAt(toUtc(request.closedAt()));
        if (request.createdAt() != null) {
            target.setCreatedAt(toUtc(request.createdAt()));
        }
        return target;
    }

    /**
     * Ejecuta la operación to utc.
     * @param value variable de entrada value.
     * @return resultado de la operación to utc.
     */

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
