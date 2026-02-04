package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz FlightOutcomeJpaRepository.
 */
public interface FlightOutcomeJpaRepository extends JpaRepository<FlightOutcomeEntity, Long> {
    /**
     * Ejecuta la operación count by actual status.
     * @param actualStatus variable de entrada actualStatus.
     * @return resultado de la operación count by actual status.
     */
    long countByActualStatus(String actualStatus);
}
