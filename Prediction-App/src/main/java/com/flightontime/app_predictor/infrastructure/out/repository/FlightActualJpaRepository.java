package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightActualEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightActualJpaRepository extends JpaRepository<FlightActualEntity, Long> {
    long countByStatus(String status);
}
