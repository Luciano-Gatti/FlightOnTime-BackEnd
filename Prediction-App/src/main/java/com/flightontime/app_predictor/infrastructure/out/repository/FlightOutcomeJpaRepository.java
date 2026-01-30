package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.FlightOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightOutcomeJpaRepository extends JpaRepository<FlightOutcomeEntity, Long> {
    long countByStatus(String status);
}
