package com.flightspredictor.flights.controller;

import com.flightspredictor.flights.domain.dto.prediction.PredictionStatsResponse;
import com.flightspredictor.flights.domain.service.prediction.PredictionStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class PredictionStatsController {

    private final PredictionStatsService predictionStatsService;

    public PredictionStatsController(PredictionStatsService predictionStatsService) {
        this.predictionStatsService = predictionStatsService;
    }

    @GetMapping
    public ResponseEntity<PredictionStatsResponse> getStats() {
        return ResponseEntity.ok(predictionStatsService.getStats());
    }
}
