package com.flightspredictor.flights.domain.entities;

import com.flightspredictor.flights.domain.enums.ActualStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "flight_outcome")
@Entity(name = "FlightOutcome")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FlightOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_request_id", nullable = false, unique = true)
    private FlightRequest flightRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "actual_status", nullable = false)
    private ActualStatus actualStatus;

    @Column(name = "actual_departure_utc")
    private LocalDateTime actualDepartureUtc;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "fetched_at", insertable = false, updatable = false)
    private LocalDateTime fetchedAt;
}
