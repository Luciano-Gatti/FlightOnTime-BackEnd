package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class FlightActualMapper {
    public FlightActual toDomain(FlightOutcomeEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlightActual(
                entity.getId(),
                entity.getRequestId(),
                entity.getFlightDate(),
                entity.getCarrier(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getFlightNumber(),
                entity.getActualDeparture(),
                entity.getActualArrival(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    public FlightOutcomeEntity toEntity(FlightActual flightActual, FlightOutcomeEntity entity) {
        if (flightActual == null) {
            return entity;
        }
        FlightOutcomeEntity target = entity == null ? new FlightOutcomeEntity() : entity;
        target.setRequestId(flightActual.requestId());
        target.setFlightDate(toUtc(flightActual.flightDate()));
        target.setCarrier(flightActual.carrier());
        target.setOrigin(flightActual.origin());
        target.setDestination(flightActual.destination());
        target.setFlightNumber(flightActual.flightNumber());
        target.setActualDeparture(toUtc(flightActual.actualDeparture()));
        target.setActualArrival(toUtc(flightActual.actualArrival()));
        target.setStatus(flightActual.status());
        if (flightActual.createdAt() != null) {
            target.setCreatedAt(toUtc(flightActual.createdAt()));
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
