package com.flightontime.app_predictor.infrastructure.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Clase AirportEntity.
 */
@Entity
@Table(name = "airports")
public class AirportEntity {
    @Id
    @Column(name = "airport_iata", nullable = false, length = 3)
    private String airportIata;

    @Column(name = "airport_name")
    private String airportName;

    @Column(name = "country")
    private String country;

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "elevation")
    private Double elevation;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "google_maps")
    private String googleMaps;

    /**
     * Ejecuta la operación get airport iata.
     * @return resultado de la operación get airport iata.
     */

    public String getAirportIata() {
        return airportIata;
    }

    /**
     * Ejecuta la operación set airport iata.
     * @param airportIata variable de entrada airportIata.
     */

    public void setAirportIata(String airportIata) {
        this.airportIata = airportIata;
    }

    /**
     * Ejecuta la operación get airport name.
     * @return resultado de la operación get airport name.
     */

    public String getAirportName() {
        return airportName;
    }

    /**
     * Ejecuta la operación set airport name.
     * @param airportName variable de entrada airportName.
     */

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    /**
     * Ejecuta la operación get country.
     * @return resultado de la operación get country.
     */

    public String getCountry() {
        return country;
    }

    /**
     * Ejecuta la operación set country.
     * @param country variable de entrada country.
     */

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Ejecuta la operación get city name.
     * @return resultado de la operación get city name.
     */

    public String getCityName() {
        return cityName;
    }

    /**
     * Ejecuta la operación set city name.
     * @param cityName variable de entrada cityName.
     */

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * Ejecuta la operación get latitude.
     * @return resultado de la operación get latitude.
     */

    public Double getLatitude() {
        return latitude;
    }

    /**
     * Ejecuta la operación set latitude.
     * @param latitude variable de entrada latitude.
     */

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Ejecuta la operación get longitude.
     * @return resultado de la operación get longitude.
     */

    public Double getLongitude() {
        return longitude;
    }

    /**
     * Ejecuta la operación set longitude.
     * @param longitude variable de entrada longitude.
     */

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Ejecuta la operación get elevation.
     * @return resultado de la operación get elevation.
     */

    public Double getElevation() {
        return elevation;
    }

    /**
     * Ejecuta la operación set elevation.
     * @param elevation variable de entrada elevation.
     */

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    /**
     * Ejecuta la operación get time zone.
     * @return resultado de la operación get time zone.
     */

    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Ejecuta la operación set time zone.
     * @param timeZone variable de entrada timeZone.
     */

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Ejecuta la operación get google maps.
     * @return resultado de la operación get google maps.
     */

    public String getGoogleMaps() {
        return googleMaps;
    }

    /**
     * Ejecuta la operación set google maps.
     * @param googleMaps variable de entrada googleMaps.
     */

    public void setGoogleMaps(String googleMaps) {
        this.googleMaps = googleMaps;
    }
}
