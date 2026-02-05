package com.flightontime.app_predictor.infrastructure.out.persistence.entities;

import com.flightontime.app_predictor.domain.model.RefreshMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "flight_subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "flight_request_id"})
})
/**
 * Clase FlightSubscriptionEntity.
 */
public class FlightSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "flight_request_id", nullable = false)
    private Long flightRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refresh_mode", nullable = false)
    private RefreshMode refreshMode;

    @Column(name = "baseline_snapshot_id")
    private Long baselineSnapshotId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Ejecuta la operación on create.
     */
    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Ejecuta la operación on update.
     */
    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
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
     * Ejecuta la operación get refresh mode.
     * @return resultado de la operación get refresh mode.
     */

    public RefreshMode getRefreshMode() {
        return refreshMode;
    }

    /**
     * Ejecuta la operación set refresh mode.
     * @param refreshMode variable de entrada refreshMode.
     */

    public void setRefreshMode(RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
    }

    /**
     * Ejecuta la operación get baseline snapshot id.
     * @return resultado de la operación get baseline snapshot id.
     */

    public Long getBaselineSnapshotId() {
        return baselineSnapshotId;
    }

    /**
     * Ejecuta la operación set baseline snapshot id.
     * @param baselineSnapshotId variable de entrada baselineSnapshotId.
     */

    public void setBaselineSnapshotId(Long baselineSnapshotId) {
        this.baselineSnapshotId = baselineSnapshotId;
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

    /**
     * Ejecuta la operación get updated at.
     * @return resultado de la operación get updated at.
     */

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Ejecuta la operación set updated at.
     * @param updatedAt variable de entrada updatedAt.
     */

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
