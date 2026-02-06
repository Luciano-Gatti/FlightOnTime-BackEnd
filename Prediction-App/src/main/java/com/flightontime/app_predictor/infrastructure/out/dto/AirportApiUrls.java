package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro AirportApiUrls.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiUrls(
        @JsonProperty("googleMaps")
        String googleMaps,
        @JsonProperty("webSite")
        String webSite,
        @JsonProperty("wikipedia")
        String wikipedia,
        @JsonProperty("twitter")
        String twitter
) {
}
