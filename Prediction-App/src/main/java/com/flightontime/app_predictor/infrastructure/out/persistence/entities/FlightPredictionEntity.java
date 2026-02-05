package com.flightontime.app_predictor.infrastructure.out.persistence.entities;

import com.flightontime.app_predictor.domain.model.PredictionSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Clase FlightPredictionEntity.
 */
@Entity
@Table(name = "flight_prediction")
public class FlightPredictionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_request_id")
    private Long flightRequestId;

    @Column(name = "forecast_bucket_utc", nullable = false)
    private OffsetDateTime forecastBucketUtc;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private PredictionSource source;

    @Column(name = "predicted_at", nullable = false)
    private OffsetDateTime predictedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Ejecuta la operación on create.
     */
    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (source == null) {
            source = PredictionSource.SYSTEM;
        }
    }

    /**
     * Ejecuta la operación get id.
     * @return resultado de la operación get id.
     */

    public Long getId() {
        return id;
    }

    /**
     * Ejecuta la operación set id.
     * @param id variable de entrada id.
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Ejecuta la operación get flight request id.
     * @return resultado de la operación get flight request id.
     */

    public Long getFlightRequestId() {
        return flightRequestId;
    }

    /**
     * Ejecuta la operación set flight request id.
     * @param flightRequestId variable de entrada flightRequestId.
     */

    public void setFlightRequestId(Long flightRequestId) {
        this.flightRequestId = flightRequestId;
    }

    /**
     * Ejecuta la operación get forecast bucket utc.
     * @return resultado de la operación get forecast bucket utc.
     */

    public OffsetDateTime getForecastBucketUtc() {
        return forecastBucketUtc;
    }

    /**
     * Ejecuta la operación set forecast bucket utc.
     * @param forecastBucketUtc variable de entrada forecastBucketUtc.
     */

    public void setForecastBucketUtc(OffsetDateTime forecastBucketUtc) {
        this.forecastBucketUtc = forecastBucketUtc;
    }

    /**
     * Ejecuta la operación get predicted status.
     * @return resultado de la operación get predicted status.
     */

    public String getPredictedStatus() {
        return predictedStatus;
    }

    /**
     * Ejecuta la operación set predicted status.
     * @param predictedStatus variable de entrada predictedStatus.
     */

    public void setPredictedStatus(String predictedStatus) {
        this.predictedStatus = predictedStatus;
    }

    /**
     * Ejecuta la operación get predicted probability.
     * @return resultado de la operación get predicted probability.
     */

    public Double getPredictedProbability() {
        return predictedProbability;
    }

    /**
     * Ejecuta la operación set predicted probability.
     * @param predictedProbability variable de entrada predictedProbability.
     */

    public void setPredictedProbability(Double predictedProbability) {
        this.predictedProbability = predictedProbability;
    }

    /**
     * Ejecuta la operación get confidence.
     * @return resultado de la operación get confidence.
     */

    public String getConfidence() {
        return confidence;
    }

    /**
     * Ejecuta la operación set confidence.
     * @param confidence variable de entrada confidence.
     */

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    /**
     * Ejecuta la operación get threshold used.
     * @return resultado de la operación get threshold used.
     */

    public Double getThresholdUsed() {
        return thresholdUsed;
    }

    /**
     * Ejecuta la operación set threshold used.
     * @param thresholdUsed variable de entrada thresholdUsed.
     */

    public void setThresholdUsed(Double thresholdUsed) {
        this.thresholdUsed = thresholdUsed;
    }

    /**
     * Ejecuta la operación get model version.
     * @return resultado de la operación get model version.
     */

    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Ejecuta la operación set model version.
     * @param modelVersion variable de entrada modelVersion.
     */

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    /**
     * Ejecuta la operación get source.
     * @return resultado de la operación get source.
     */

    public PredictionSource getSource() {
        return source;
    }

    /**
     * Ejecuta la operación set source.
     * @param source variable de entrada source.
     */

    public void setSource(PredictionSource source) {
        this.source = source;
    }

    /**
     * Ejecuta la operación get predicted at.
     * @return resultado de la operación get predicted at.
     */

    public OffsetDateTime getPredictedAt() {
        return predictedAt;
    }

    /**
     * Ejecuta la operación set predicted at.
     * @param predictedAt variable de entrada predictedAt.
     */

    public void setPredictedAt(OffsetDateTime predictedAt) {
        this.predictedAt = predictedAt;
    }

    /**
     * Ejecuta la operación get created at.
     * @return resultado de la operación get created at.
     */

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ejecuta la operación set created at.
     * @param createdAt variable de entrada createdAt.
     */

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
