package com.flightspredictor.flights.infra.external.airports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightspredictor.flights.infra.external.airports.dto.Country;
import com.flightspredictor.flights.infra.external.airports.dto.Elevation;
import com.flightspredictor.flights.infra.external.airports.dto.GoogleMaps;
import com.flightspredictor.flights.infra.external.airports.dto.Location;
import lombok.Getter;

/*
* Clase intermedia para representar mejor el JSON de respuesta de la API
* y manejar mejor la deserialización de campos necesarios para su almacenamiento
* posterior en la base de datos.
 * */

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirportResponse {
    @JsonProperty("iata")
    String airportIata;

    @JsonProperty("fullName")
    String airportName;

    Country country;

    @JsonProperty("municipalityName")
    String cityName;

    Location location;
    Elevation elevation;

    @JsonProperty("timeZone")
    String timezone;

    GoogleMaps googleMaps;
}
