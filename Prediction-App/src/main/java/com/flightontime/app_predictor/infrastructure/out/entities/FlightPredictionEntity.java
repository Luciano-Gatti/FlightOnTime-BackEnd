package com.flightontime.app_predictor.infrastructure.out.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "flight_prediction")
public class FlightPredictionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_request_id")
    private Long flightRequestId;

    @Column(name = "predicted_status", nullable = false)
    private String predictedStatus;

    @Column(name = "predicted_probability")
    private Double predictedProbability;

    @Column(name = "confidence", nullable = true)
    private String confidence;

    @Column(name = "threshold_used")
    private Double thresholdUsed;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "predicted_at", nullable = false)
    private OffsetDateTime predictedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlightRequestId() {
        return flightRequestId;
    }

    public void setFlightRequestId(Long flightRequestId) {
        this.flightRequestId = flightRequestId;
    }

    public String getPredictedStatus() {
        return predictedStatus;
    }

    public void setPredictedStatus(String predictedStatus) {
        this.predictedStatus = predictedStatus;
    }

    public Double getPredictedProbability() {
        return predictedProbability;
    }

    public void setPredictedProbability(Double predictedProbability) {
        this.predictedProbability = predictedProbability;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public Double getThresholdUsed() {
        return thresholdUsed;
    }

    public void setThresholdUsed(Double thresholdUsed) {
        this.thresholdUsed = thresholdUsed;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public OffsetDateTime getPredictedAt() {
        return predictedAt;
    }

    public void setPredictedAt(OffsetDateTime predictedAt) {
        this.predictedAt = predictedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
