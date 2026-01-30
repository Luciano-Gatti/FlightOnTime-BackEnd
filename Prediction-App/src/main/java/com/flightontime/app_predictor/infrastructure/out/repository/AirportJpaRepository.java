package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.AirportEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportJpaRepository extends JpaRepository<AirportEntity, String> {
    Optional<AirportEntity> findByAirportIata(String airportIata);
}
