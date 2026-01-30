package com.flightontime.app_predictor.infrastructure.out.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "airports")
public class AirportEntity {
    @Id
    @Column(name = "airport_iata", nullable = false)
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

    protected AirportEntity() {
    }

    public AirportEntity(
            String airportIata,
            String airportName,
            String country,
            String cityName,
            Double latitude,
            Double longitude,
            Double elevation,
            String timeZone,
            String googleMaps
    ) {
        this.airportIata = airportIata;
        this.airportName = airportName;
        this.country = country;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.timeZone = timeZone;
        this.googleMaps = googleMaps;
    }

    public String getAirportIata() {
        return airportIata;
    }

    public void setAirportIata(String airportIata) {
        this.airportIata = airportIata;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getGoogleMaps() {
        return googleMaps;
    }

    public void setGoogleMaps(String googleMaps) {
        this.googleMaps = googleMaps;
    }
}
