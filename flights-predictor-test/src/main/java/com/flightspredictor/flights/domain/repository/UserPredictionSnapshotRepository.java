package com.flightspredictor.flights.domain.repository;

import com.flightspredictor.flights.domain.entities.UserPredictionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPredictionSnapshotRepository extends JpaRepository<UserPredictionSnapshot, Long> {
    boolean existsByUserIdAndFlightPredictionId(Long userId, Long flightPredictionId);
}
