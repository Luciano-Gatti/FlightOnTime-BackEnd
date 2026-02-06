package com.flightspredictor.flights.domain.entities;

import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.enums.PredictionSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "flight_prediction")
@Entity(name = "FlightPrediction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FlightPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_request_id", nullable = false)
    private FlightRequest flightRequest;

    @Column(name = "forecast_bucket_utc", nullable = false)
    private LocalDateTime forecastBucketUtc;

    @Column(name = "predicted_at", nullable = false)
    private LocalDateTime predictedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "predicted_status", nullable = false)
    private PredictedStatus predictedStatus;

    @Column(name = "predicted_probability")
    private Double predictedProbability;

    @Column(name = "confidence")
    private String confidence;

    @Column(name = "threshold_used")
    private Double thresholdUsed;

    @Column(name = "model_version")
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private PredictionSource source;

    public FlightPrediction(ModelPredictionResponse data, FlightRequest flightRequest) {
        this.id = null;
        this.flightRequest = flightRequest;
        this.forecastBucketUtc = flightRequest.getFlightDateUtc();
        this.predictedAt = LocalDateTime.now();
        this.predictedStatus = data.predictedStatus();
        this.predictedProbability = data.predictedProbability();
        this.confidence = data.confidence();
        this.thresholdUsed = null;
        this.modelVersion = null;
        this.source = PredictionSource.SYSTEM;
    }
}
