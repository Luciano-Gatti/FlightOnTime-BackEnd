package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightOutcomeEntity;
import java.util.Optional;
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

    /**
     * Ejecuta la operación find by flight request id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find by flight request id.
     */
    Optional<FlightOutcomeEntity> findByFlightRequestId(Long flightRequestId);
}
