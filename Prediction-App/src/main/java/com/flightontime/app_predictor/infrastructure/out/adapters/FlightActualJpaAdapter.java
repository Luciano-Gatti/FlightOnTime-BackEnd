package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightOutcomeJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Clase FlightActualJpaAdapter.
 */
@Component
public class FlightActualJpaAdapter implements FlightActualRepositoryPort {
    private final FlightOutcomeJpaRepository flightOutcomeJpaRepository;
    private final FlightActualMapper flightActualMapper = new FlightActualMapper();

    /**
     * Ejecuta la operación flight actual jpa adapter.
     * @param flightOutcomeJpaRepository variable de entrada flightOutcomeJpaRepository.
     */

    /**
     * Ejecuta la operación flight actual jpa adapter.
     * @param flightOutcomeJpaRepository variable de entrada flightOutcomeJpaRepository.
     * @return resultado de la operación flight actual jpa adapter.
     */

    public FlightActualJpaAdapter(FlightOutcomeJpaRepository flightOutcomeJpaRepository) {
        this.flightOutcomeJpaRepository = flightOutcomeJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param flightActual variable de entrada flightActual.
     * @return resultado de la operación save.
     */
    @Override
    public FlightActual save(FlightActual flightActual) {
        if (flightActual == null) {
            throw new IllegalArgumentException("Flight actual is required");
        }
        FlightOutcomeEntity entity = resolveEntity(flightActual.id());
        flightActualMapper.toEntity(flightActual, entity);
        return flightActualMapper.toDomain(flightOutcomeJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */
    @Override
    public Optional<FlightActual> findById(Long id) {
        return flightOutcomeJpaRepository.findById(id)
                .map(flightActualMapper::toDomain);
    }

    /**
     * Ejecuta la operación count all.
     * @return resultado de la operación count all.
     */
    @Override
    public long countAll() {
        return flightOutcomeJpaRepository.count();
    }

    /**
     * Ejecuta la operación count by actual status.
     * @param actualStatus variable de entrada actualStatus.
     * @return resultado de la operación count by actual status.
     */
    @Override
    public long countByActualStatus(String actualStatus) {
        return flightOutcomeJpaRepository.countByActualStatus(actualStatus);
    }

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private FlightOutcomeEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightOutcomeEntity();
        }
        return flightOutcomeJpaRepository.findById(id).orElseGet(FlightOutcomeEntity::new);
    }
}
