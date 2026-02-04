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
     * Ejecuta la operación get actual departure.
     * @return resultado de la operación get actual departure.
     */

    public OffsetDateTime getActualDeparture() {
        return actualDeparture;
    }

    /**
     * Ejecuta la operación set actual departure.
     * @param actualDeparture variable de entrada actualDeparture.
     */

    public void setActualDeparture(OffsetDateTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    /**
     * Ejecuta la operación get actual arrival.
     * @return resultado de la operación get actual arrival.
     */

    public OffsetDateTime getActualArrival() {
        return actualArrival;
    }

    /**
     * Ejecuta la operación set actual arrival.
     * @param actualArrival variable de entrada actualArrival.
     */

    public void setActualArrival(OffsetDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }

    /**
     * Ejecuta la operación get actual status.
     * @return resultado de la operación get actual status.
     */

    public String getActualStatus() {
        return actualStatus;
    }

    /**
     * Ejecuta la operación set actual status.
     * @param actualStatus variable de entrada actualStatus.
     */

    public void setActualStatus(String actualStatus) {
        this.actualStatus = actualStatus;
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
