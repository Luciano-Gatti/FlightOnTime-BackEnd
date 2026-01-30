package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiResponse(
        @JsonProperty("iata")
        String airportIata,
        @JsonProperty("fullName")
        String airportName,
        AirportApiCountry country,
        @JsonProperty("municipalityName")
        String cityName,
        AirportApiLocation location,
        AirportApiElevation elevation,
        @JsonProperty("timeZone")
        String timeZone,
        AirportApiGoogleMaps googleMaps
) {
}
