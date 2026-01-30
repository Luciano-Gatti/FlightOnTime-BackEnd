package com.flightspredictor.flights.domain.repository;

import com.flightspredictor.flights.domain.entities.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
}
