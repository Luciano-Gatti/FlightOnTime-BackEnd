package com.flightontime.app_predictor.infrastructure.out.entities;

import com.flightontime.app_predictor.domain.model.UserPredictionSource;
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
 * Clase UserPredictionSnapshotEntity.
 */
@Entity
@Table(name = "user_prediction_snapshot")
public class UserPredictionSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "flight_request_id", nullable = false)
    private Long flightRequestId;

    @Column(name = "flight_prediction_id", nullable = false)
    private Long flightPredictionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private UserPredictionSource source;

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
            source = UserPredictionSource.USER_QUERY;
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
     * Ejecuta la operación get user id.
     * @return resultado de la operación get user id.
     */

    public Long getUserId() {
        return userId;
    }

    /**
     * Ejecuta la operación set user id.
     * @param userId variable de entrada userId.
     */

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Ejecuta la operación get flight prediction id.
     * @return resultado de la operación get flight prediction id.
     */

    public Long getFlightPredictionId() {
        return flightPredictionId;
    }

    /**
     * Ejecuta la operación set flight prediction id.
     * @param flightPredictionId variable de entrada flightPredictionId.
     */

    public void setFlightPredictionId(Long flightPredictionId) {
        this.flightPredictionId = flightPredictionId;
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
     * Ejecuta la operación get source.
     * @return resultado de la operación get source.
     */

    public UserPredictionSource getSource() {
        return source;
    }

    /**
     * Ejecuta la operación set source.
     * @param source variable de entrada source.
     */

    public void setSource(UserPredictionSource source) {
        this.source = source;
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
