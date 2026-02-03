package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.domain.ports.in.StatsAccuracyByLeadTimeUseCase;
import com.flightontime.app_predictor.domain.ports.in.StatsSummaryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsSummaryResponseDTO;
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
        return ResponseEntity.ok(statsSummaryUseCase.getSummary(topN));
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
    public ResponseEntity<StatsAccuracyByLeadTimeResponseDTO> getAccuracyByLeadTime() {
        return ResponseEntity.ok(statsAccuracyByLeadTimeUseCase.getAccuracyByLeadTime());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

/**
 * Registro ErrorResponse.
 */
    public record ErrorResponse(String message) {
    }
}
