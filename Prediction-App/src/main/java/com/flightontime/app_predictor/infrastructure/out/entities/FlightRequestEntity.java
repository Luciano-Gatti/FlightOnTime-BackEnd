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

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (active == null) {
            active = Boolean.TRUE;
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

    public OffsetDateTime getFlightDateUtc() {
        return flightDateUtc;
    }

    public void setFlightDateUtc(OffsetDateTime flightDateUtc) {
        this.flightDateUtc = flightDateUtc;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getOriginIata() {
        return originIata;
    }

    public void setOriginIata(String originIata) {
        this.originIata = originIata;
    }

    public String getDestIata() {
        return destIata;
    }

    public void setDestIata(String destIata) {
        this.destIata = destIata;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
