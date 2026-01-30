package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record PredictRequestDTO(
        @NotNull
        @Future
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime flDate,
        @NotBlank
        String carrier,
        @NotBlank
        @Size(min = 3, max = 3)
        String origin,
        @NotBlank
        @Size(min = 3, max = 3)
        String dest,
        String flightNumber
) {
}
