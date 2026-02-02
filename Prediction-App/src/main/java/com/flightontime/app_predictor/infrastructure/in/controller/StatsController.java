package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.domain.ports.in.StatsAccuracyByLeadTimeUseCase;
import com.flightontime.app_predictor.domain.ports.in.StatsSummaryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsSummaryResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Estadisticas", description = "Endpoints para consultar estadisticas del sistema")
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
    public ResponseEntity<StatsSummaryResponseDTO> getSummary(
            @RequestParam(name = "topN", defaultValue = "5") int topN
    ) {
        if (topN < 0) {
            throw new IllegalArgumentException("topN must be greater than or equal to 0");
        }
        return ResponseEntity.ok(statsSummaryUseCase.getSummary(topN));
    }

    @GetMapping("/accuracy-by-leadtime")
    public ResponseEntity<StatsAccuracyByLeadTimeResponseDTO> getAccuracyByLeadTime() {
        return ResponseEntity.ok(statsAccuracyByLeadTimeUseCase.getAccuracyByLeadTime());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    public record ErrorResponse(String message) {
    }
}
