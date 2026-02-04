package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.domain.model.StatsAccuracyBin;
import com.flightontime.app_predictor.domain.model.StatsAccuracyByLeadTime;
import com.flightontime.app_predictor.domain.model.StatsSummary;
import com.flightontime.app_predictor.domain.model.StatsTopFlight;
import com.flightontime.app_predictor.domain.ports.in.StatsAccuracyByLeadTimeUseCase;
import com.flightontime.app_predictor.domain.ports.in.StatsSummaryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyBinDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsSummaryResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsTopFlightDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase StatsController.
 */
@RestController
@RequestMapping("/stats")
@Tag(name = "Stats", description = "Endpoints para consultar estadísticas del sistema")
public class StatsController {
    private final StatsAccuracyByLeadTimeUseCase statsAccuracyByLeadTimeUseCase;
    private final StatsSummaryUseCase statsSummaryUseCase;

    /**
     * Ejecuta la operación stats controller.
     * @param statsAccuracyByLeadTimeUseCase variable de entrada statsAccuracyByLeadTimeUseCase.
     * @param statsSummaryUseCase variable de entrada statsSummaryUseCase.
     */

    /**
     * Ejecuta la operación stats controller.
     * @param statsAccuracyByLeadTimeUseCase variable de entrada statsAccuracyByLeadTimeUseCase.
     * @param statsSummaryUseCase variable de entrada statsSummaryUseCase.
     * @return resultado de la operación stats controller.
     */

    public StatsController(
            StatsAccuracyByLeadTimeUseCase statsAccuracyByLeadTimeUseCase,
            StatsSummaryUseCase statsSummaryUseCase
    ) {
        this.statsAccuracyByLeadTimeUseCase = statsAccuracyByLeadTimeUseCase;
        this.statsSummaryUseCase = statsSummaryUseCase;
    }

    @GetMapping("/summary")
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Resumen de estadísticas",
            description = "Devuelve métricas agregadas y top vuelos consultados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen generado"),
            @ApiResponse(responseCode = "400", description = "Parámetro inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<StatsSummaryResponseDTO> getSummary(
            @Parameter(description = "Cantidad de vuelos a incluir en el top", example = "5")
            @RequestParam(name = "topN", defaultValue = "5") int topN
    ) {
        if (topN < 0) {
            throw new IllegalArgumentException("topN must be greater than or equal to 0");
        }
        StatsSummary summary = statsSummaryUseCase.getSummary(topN);
        return ResponseEntity.ok(toSummaryDto(summary));
    }

    @GetMapping("/accuracy-by-leadtime")
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Precisión por lead time",
            description = "Entrega la precisión del modelo segmentada por lead time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas generadas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    /**
     * Ejecuta la operación get accuracy by lead time.
     * @return resultado de la operación get accuracy by lead time.
     */
    public ResponseEntity<StatsAccuracyByLeadTimeResponseDTO> getAccuracyByLeadTime() {
        StatsAccuracyByLeadTime accuracy = statsAccuracyByLeadTimeUseCase.getAccuracyByLeadTime();
        return ResponseEntity.ok(toAccuracyDto(accuracy));
    }

    /**
     * Ejecuta la operación handle validation error.
     * @param ex variable de entrada ex.
     * @return resultado de la operación handle validation error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

/**
 * Registro ErrorResponse.
     * @param message variable de entrada message.
     * @return resultado de la operación resultado.
 */
    public record ErrorResponse(String message) {
    }

    /**
     * Ejecuta la operación to summary dto.
     * @param summary variable de entrada summary.
     * @return resultado de la operación to summary dto.
     */

    private StatsSummaryResponseDTO toSummaryDto(StatsSummary summary) {
        if (summary == null) {
            return null;
        }
        return new StatsSummaryResponseDTO(
                summary.totalPredictions(),
                summary.totalOnTimePredicted(),
                summary.totalDelayedPredicted(),
                summary.totalFlightsWithActual(),
                summary.totalCancelled(),
                summary.topFlights().stream()
                        .map(this::toTopFlightDto)
                        .toList()
        );
    }

    /**
     * Ejecuta la operación to top flight dto.
     * @param flight variable de entrada flight.
     * @return resultado de la operación to top flight dto.
     */

    private StatsTopFlightDTO toTopFlightDto(StatsTopFlight flight) {
        if (flight == null) {
            return null;
        }
        return new StatsTopFlightDTO(
                flight.flightRequestId(),
                flight.flightDateUtc(),
                flight.airlineCode(),
                flight.originIata(),
                flight.destIata(),
                flight.flightNumber(),
                flight.uniqueUsersCount()
        );
    }

    /**
     * Ejecuta la operación to accuracy dto.
     * @param accuracy variable de entrada accuracy.
     * @return resultado de la operación to accuracy dto.
     */

    private StatsAccuracyByLeadTimeResponseDTO toAccuracyDto(StatsAccuracyByLeadTime accuracy) {
        if (accuracy == null) {
            return null;
        }
        return new StatsAccuracyByLeadTimeResponseDTO(
                accuracy.bins().stream()
                        .map(this::toAccuracyBinDto)
                        .toList()
        );
    }

    /**
     * Ejecuta la operación to accuracy bin dto.
     * @param bin variable de entrada bin.
     * @return resultado de la operación to accuracy bin dto.
     */

    private StatsAccuracyBinDTO toAccuracyBinDto(StatsAccuracyBin bin) {
        if (bin == null) {
            return null;
        }
        return new StatsAccuracyBinDTO(
                bin.leadTimeHours(),
                bin.total(),
                bin.correct(),
                bin.accuracy()
        );
    }
}
