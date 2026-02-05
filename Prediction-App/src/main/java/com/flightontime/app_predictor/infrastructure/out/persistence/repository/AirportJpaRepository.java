package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

import com.flightontime.app_predictor.infrastructure.out.persistence.entities.AirportEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz AirportJpaRepository.
 */
public interface AirportJpaRepository extends JpaRepository<AirportEntity, String> {
    /**
     * Ejecuta la operación find by airport iata.
     * @param airportIata variable de entrada airportIata.
     * @return resultado de la operación find by airport iata.
     */
    Optional<AirportEntity> findByAirportIata(String airportIata);
}
