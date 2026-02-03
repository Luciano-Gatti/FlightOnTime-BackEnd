package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz FlightOutcomeJpaRepository.
 */
public interface FlightOutcomeJpaRepository extends JpaRepository<FlightOutcomeEntity, Long> {
    long countByActualStatus(String actualStatus);
}
