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
 * Clase FlightOutcomeEntity.
 */
@Entity
@Table(name = "flight_outcome")
public class FlightOutcomeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_request_id")
    private Long flightRequestId;

    @Column(name = "flight_date_utc", nullable = false)
    private OffsetDateTime flightDateUtc;

    @Column(name = "airline_code", nullable = false)
    private String airlineCode;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "dest_iata", nullable = false, length = 3)
    private String destIata;

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "actual_departure")
    private OffsetDateTime actualDeparture;

    @Column(name = "actual_arrival")
    private OffsetDateTime actualArrival;

    @Column(name = "actual_status")
    private String actualStatus;

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

    public OffsetDateTime getActualDeparture() {
        return actualDeparture;
    }

    public void setActualDeparture(OffsetDateTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public OffsetDateTime getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(OffsetDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }

    public String getActualStatus() {
        return actualStatus;
    }

    public void setActualStatus(String actualStatus) {
        this.actualStatus = actualStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
