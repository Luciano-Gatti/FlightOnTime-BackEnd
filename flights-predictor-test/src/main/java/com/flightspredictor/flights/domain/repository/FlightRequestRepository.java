package com.flightspredictor.flights.domain.repository;

import com.flightspredictor.flights.domain.entities.FlightRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRequestRepository extends JpaRepository<FlightRequest, Long> {

    Optional<FlightRequest> findByFlightDateUtcAndAirlineCodeAndOriginIataAndDestIata(
            LocalDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata
    );
}
