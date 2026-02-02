package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record PredictRequestDTO(
        @NotNull
        @Future
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("flDate")
        OffsetDateTime flightDateUtc,
        @NotBlank
        @JsonProperty("carrier")
        String airlineCode,
        @NotBlank
        @Size(min = 3, max = 3)
        @JsonProperty("origin")
        String originIata,
        @NotBlank
        @Size(min = 3, max = 3)
        @JsonProperty("dest")
        String destIata,
        String flightNumber
) {
}
