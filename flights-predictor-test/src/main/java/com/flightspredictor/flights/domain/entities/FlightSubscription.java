package com.flightspredictor.flights.domain.entities;

import com.flightspredictor.flights.domain.enums.RefreshMode;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "flight_subscription")
@Entity(name = "FlightSubscription")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FlightSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_request_id", nullable = false)
    private FlightRequest flightRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "refresh_mode", nullable = false)
    private RefreshMode refreshMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baseline_snapshot_id")
    private UserPredictionSnapshot baselineSnapshot;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
