package com.flightspredictor.flights.domain.entities;

import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "flight_request")
@Entity(name = "FlightRequest")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FlightRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_date_utc", nullable = false)
    private LocalDateTime flightDateUtc;

    @Column(name = "airline_code", nullable = false, length = 2)
    private String airlineCode;

    @Column(name = "flight_number", length = 10)
    private String flightNumber;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "dest_iata", nullable = false, length = 3)
    private String destIata;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "flightRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlightPrediction> predictions;

    @OneToOne(mappedBy = "flightRequest", fetch = FetchType.LAZY)
    private FlightOutcome flightOutcome;

    public FlightRequest(PredictionRequest data, Double calculatedDistance) {
        this.id = null;
        this.flightDateUtc = data.flightDateTime().toLocalDateTime();
        this.airlineCode = data.opUniqueCarrier();
        this.flightNumber = null;
        this.originIata = data.origin();
        this.destIata = data.dest();
        this.distance = calculatedDistance;
        this.active = true;
        this.closedAt = null;
        this.createdAt = null;
    }

    public void setPredictions(List<FlightPrediction> predictions) {
        this.predictions = predictions;
    }
}
