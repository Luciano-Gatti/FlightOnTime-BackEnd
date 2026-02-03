package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Registro AirportApiSearchResponse.
 */
public record AirportApiSearchResponse(
        @JsonProperty("data")
        List<AirportApiResponse> data
) {
}
