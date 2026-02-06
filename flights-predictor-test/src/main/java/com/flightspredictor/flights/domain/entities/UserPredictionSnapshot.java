package com.flightspredictor.flights.domain.entities;

import com.flightspredictor.flights.domain.enums.SnapshotSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_prediction_snapshot")
@Entity(name = "UserPredictionSnapshot")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserPredictionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_request_id", nullable = false)
    private FlightRequest flightRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_prediction_id", nullable = false)
    private FlightPrediction flightPrediction;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private SnapshotSource source;
}
