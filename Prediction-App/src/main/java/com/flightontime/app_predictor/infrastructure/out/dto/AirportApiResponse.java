package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AirportApiResponse(
        @JsonProperty("airport_iata")
        String airportIata,
        @JsonProperty("airport_name")
        String airportName,
        @JsonProperty("country")
        String country,
        @JsonProperty("city_name")
        String cityName,
        @JsonProperty("latitude")
        Double latitude,
        @JsonProperty("longitude")
        Double longitude,
        @JsonProperty("elevation")
        Double elevation,
        @JsonProperty("time_zone")
        String timeZone,
        @JsonProperty("google_maps")
        String googleMaps
) {
}
