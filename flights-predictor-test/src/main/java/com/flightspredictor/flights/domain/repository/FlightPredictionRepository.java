package com.flightspredictor.flights.domain.repository;

import com.flightspredictor.flights.domain.entities.FlightPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightPredictionRepository extends JpaRepository<FlightPrediction, Long> {
}
