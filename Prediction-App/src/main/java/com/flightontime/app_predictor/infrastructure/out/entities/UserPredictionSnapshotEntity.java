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

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (source == null) {
            source = UserPredictionSource.USER_QUERY;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFlightPredictionId() {
        return flightPredictionId;
    }

    public void setFlightPredictionId(Long flightPredictionId) {
        this.flightPredictionId = flightPredictionId;
    }

    public Long getFlightRequestId() {
        return flightRequestId;
    }

    public void setFlightRequestId(Long flightRequestId) {
        this.flightRequestId = flightRequestId;
    }

    public UserPredictionSource getSource() {
        return source;
    }

    public void setSource(UserPredictionSource source) {
        this.source = source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
