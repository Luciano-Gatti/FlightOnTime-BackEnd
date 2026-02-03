package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * Registro PredictRequestDTO.
 */
public record PredictRequestDTO(
        @NotNull
        @Future
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("flDate")
        @Schema(description = "Fecha/hora UTC del vuelo", example = "2025-05-01T14:30:00Z")
        OffsetDateTime flightDateUtc,
        @NotBlank
        @JsonProperty("carrier")
        @Schema(description = "Código de aerolínea", example = "AA")
        String airlineCode,
        @NotBlank
        @Size(min = 3, max = 3)
        @JsonProperty("origin")
        @Schema(description = "IATA aeropuerto origen", example = "EZE")
        String originIata,
        @NotBlank
        @Size(min = 3, max = 3)
        @JsonProperty("dest")
        @Schema(description = "IATA aeropuerto destino", example = "JFK")
        String destIata,
        @Schema(description = "Número de vuelo (opcional)", example = "102")
        String flightNumber
) {
}
