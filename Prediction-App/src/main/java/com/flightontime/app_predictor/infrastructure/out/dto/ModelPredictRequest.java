package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record ModelPredictRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("flDate")
        OffsetDateTime flightDateUtc,
        @JsonProperty("carrier")
        String airlineCode,
        @JsonProperty("origin")
        String originIata,
        @JsonProperty("dest")
        String destIata,
        String flightNumber,
        double distance
) {
}
