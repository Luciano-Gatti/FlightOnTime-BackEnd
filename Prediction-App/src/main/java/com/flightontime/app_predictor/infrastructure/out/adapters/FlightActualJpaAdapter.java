package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightOutcomeJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FlightActualJpaAdapter implements FlightActualRepositoryPort {
    private final FlightOutcomeJpaRepository flightOutcomeJpaRepository;
    private final FlightActualMapper flightActualMapper = new FlightActualMapper();

    public FlightActualJpaAdapter(FlightOutcomeJpaRepository flightOutcomeJpaRepository) {
        this.flightOutcomeJpaRepository = flightOutcomeJpaRepository;
    }

    @Override
    public FlightActual save(FlightActual flightActual) {
        if (flightActual == null) {
            throw new IllegalArgumentException("Flight actual is required");
        }
        FlightOutcomeEntity entity = resolveEntity(flightActual.id());
        flightActualMapper.toEntity(flightActual, entity);
        return flightActualMapper.toDomain(flightOutcomeJpaRepository.save(entity));
    }

    @Override
    public Optional<FlightActual> findById(Long id) {
        return flightOutcomeJpaRepository.findById(id)
                .map(flightActualMapper::toDomain);
    }

    @Override
    public long countAll() {
        return flightOutcomeJpaRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return flightOutcomeJpaRepository.countByStatus(status);
    }

    private FlightOutcomeEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightOutcomeEntity();
        }
        return flightOutcomeJpaRepository.findById(id).orElseGet(FlightOutcomeEntity::new);
    }
}
