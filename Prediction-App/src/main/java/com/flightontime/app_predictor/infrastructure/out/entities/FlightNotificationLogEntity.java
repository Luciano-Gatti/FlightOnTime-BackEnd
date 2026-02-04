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

/**
 * Clase FlightNotificationLogEntity.
 */
@Entity
@Table(name = "flight_notification_log")
public class FlightNotificationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_prediction_id", nullable = false)
    private Long userPredictionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "message")
    private String message;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

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
     * Ejecuta la operación get user prediction id.
     * @return resultado de la operación get user prediction id.
     */

    public Long getUserPredictionId() {
        return userPredictionId;
    }

    /**
     * Ejecuta la operación set user prediction id.
     * @param userPredictionId variable de entrada userPredictionId.
     */

    public void setUserPredictionId(Long userPredictionId) {
        this.userPredictionId = userPredictionId;
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
     * Ejecuta la operación get request id.
     * @return resultado de la operación get request id.
     */

    public Long getRequestId() {
        return requestId;
    }

    /**
     * Ejecuta la operación set request id.
     * @param requestId variable de entrada requestId.
     */

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    /**
     * Ejecuta la operación get type.
     * @return resultado de la operación get type.
     */

    public String getType() {
        return type;
    }

    /**
     * Ejecuta la operación set type.
     * @param type variable de entrada type.
     */

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Ejecuta la operación get channel.
     * @return resultado de la operación get channel.
     */

    public String getChannel() {
        return channel;
    }

    /**
     * Ejecuta la operación set channel.
     * @param channel variable de entrada channel.
     */

    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Ejecuta la operación get status.
     * @return resultado de la operación get status.
     */

    public String getStatus() {
        return status;
    }

    /**
     * Ejecuta la operación set status.
     * @param status variable de entrada status.
     */

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Ejecuta la operación get message.
     * @return resultado de la operación get message.
     */

    public String getMessage() {
        return message;
    }

    /**
     * Ejecuta la operación set message.
     * @param message variable de entrada message.
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Ejecuta la operación get sent at.
     * @return resultado de la operación get sent at.
     */

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Ejecuta la operación set sent at.
     * @param sentAt variable de entrada sentAt.
     */

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
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
