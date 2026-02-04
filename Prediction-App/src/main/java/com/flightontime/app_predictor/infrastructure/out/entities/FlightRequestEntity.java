package com.flightontime.app_predictor.infrastructure.out.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "flight_request",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"flight_date_utc", "airline_code", "origin_iata", "dest_iata"})
        }
)
/**
 * Clase FlightRequestEntity.
 */
public class FlightRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private Long userId;

    @Column(name = "flight_date_utc", nullable = false)
    private OffsetDateTime flightDateUtc;

    @Column(name = "airline_code", nullable = false)
    private String airlineCode;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "dest_iata", nullable = false, length = 3)
    private String destIata;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

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
        if (active == null) {
            active = Boolean.TRUE;
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
     * Ejecuta la operación get flight date utc.
     * @return resultado de la operación get flight date utc.
     */

    public OffsetDateTime getFlightDateUtc() {
        return flightDateUtc;
    }

    /**
     * Ejecuta la operación set flight date utc.
     * @param flightDateUtc variable de entrada flightDateUtc.
     */

    public void setFlightDateUtc(OffsetDateTime flightDateUtc) {
        this.flightDateUtc = flightDateUtc;
    }

    /**
     * Ejecuta la operación get airline code.
     * @return resultado de la operación get airline code.
     */

    public String getAirlineCode() {
        return airlineCode;
    }

    /**
     * Ejecuta la operación set airline code.
     * @param airlineCode variable de entrada airlineCode.
     */

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    /**
     * Ejecuta la operación get origin iata.
     * @return resultado de la operación get origin iata.
     */

    public String getOriginIata() {
        return originIata;
    }

    /**
     * Ejecuta la operación set origin iata.
     * @param originIata variable de entrada originIata.
     */

    public void setOriginIata(String originIata) {
        this.originIata = originIata;
    }

    /**
     * Ejecuta la operación get dest iata.
     * @return resultado de la operación get dest iata.
     */

    public String getDestIata() {
        return destIata;
    }

    /**
     * Ejecuta la operación set dest iata.
     * @param destIata variable de entrada destIata.
     */

    public void setDestIata(String destIata) {
        this.destIata = destIata;
    }

    /**
     * Ejecuta la operación get flight number.
     * @return resultado de la operación get flight number.
     */

    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * Ejecuta la operación set flight number.
     * @param flightNumber variable de entrada flightNumber.
     */

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    /**
     * Ejecuta la operación get distance.
     * @return resultado de la operación get distance.
     */

    public Double getDistance() {
        return distance;
    }

    /**
     * Ejecuta la operación set distance.
     * @param distance variable de entrada distance.
     */

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * Ejecuta la operación get active.
     * @return resultado de la operación get active.
     */

    public Boolean getActive() {
        return active;
    }

    /**
     * Ejecuta la operación set active.
     * @param active variable de entrada active.
     */

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Ejecuta la operación get closed at.
     * @return resultado de la operación get closed at.
     */

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    /**
     * Ejecuta la operación set closed at.
     * @param closedAt variable de entrada closedAt.
     */

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
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
