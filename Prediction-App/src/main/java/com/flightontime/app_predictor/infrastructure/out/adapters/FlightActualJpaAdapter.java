package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightActualEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightActualJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FlightActualJpaAdapter implements FlightActualRepositoryPort {
    private final FlightActualJpaRepository flightActualJpaRepository;
    private final FlightActualMapper flightActualMapper = new FlightActualMapper();

    public FlightActualJpaAdapter(FlightActualJpaRepository flightActualJpaRepository) {
        this.flightActualJpaRepository = flightActualJpaRepository;
    }

    @Override
    public FlightActual save(FlightActual flightActual) {
        if (flightActual == null) {
            throw new IllegalArgumentException("Flight actual is required");
        }
        FlightActualEntity entity = resolveEntity(flightActual.id());
        flightActualMapper.toEntity(flightActual, entity);
        return flightActualMapper.toDomain(flightActualJpaRepository.save(entity));
    }

    @Override
    public Optional<FlightActual> findById(Long id) {
        return flightActualJpaRepository.findById(id)
                .map(flightActualMapper::toDomain);
    }

    private FlightActualEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightActualEntity();
        }
        return flightActualJpaRepository.findById(id).orElseGet(FlightActualEntity::new);
    }
}
