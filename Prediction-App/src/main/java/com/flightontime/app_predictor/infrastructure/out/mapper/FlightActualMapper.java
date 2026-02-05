package com.flightontime.app_predictor.infrastructure.out.mapper;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightOutcomeEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Clase FlightActualMapper.
 */
public class FlightActualMapper {
    /**
     * Ejecuta la operación to domain.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to domain.
     */
    public FlightActual toDomain(FlightOutcomeEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlightActual(
                entity.getId(),
                entity.getFlightRequestId(),
                entity.getFlightDateUtc(),
                entity.getAirlineCode(),
                entity.getOriginIata(),
                entity.getDestIata(),
                entity.getFlightNumber(),
                entity.getActualDeparture(),
                entity.getActualArrival(),
                entity.getActualStatus(),
                entity.getCreatedAt()
        );
    }

    /**
     * Ejecuta la operación to entity.
     * @param flightActual variable de entrada flightActual.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to entity.
     */

    public FlightOutcomeEntity toEntity(FlightActual flightActual, FlightOutcomeEntity entity) {
        if (flightActual == null) {
            return entity;
        }
        FlightOutcomeEntity target = entity == null ? new FlightOutcomeEntity() : entity;
        target.setFlightRequestId(flightActual.flightRequestId());
        target.setFlightDateUtc(toUtc(flightActual.flightDateUtc()));
        target.setAirlineCode(flightActual.airlineCode());
        target.setOriginIata(flightActual.originIata());
        target.setDestIata(flightActual.destIata());
        target.setFlightNumber(flightActual.flightNumber());
        target.setActualDeparture(toUtc(flightActual.actualDeparture()));
        target.setActualArrival(toUtc(flightActual.actualArrival()));
        target.setActualStatus(flightActual.actualStatus());
        if (flightActual.createdAt() != null) {
            target.setCreatedAt(toUtc(flightActual.createdAt()));
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
