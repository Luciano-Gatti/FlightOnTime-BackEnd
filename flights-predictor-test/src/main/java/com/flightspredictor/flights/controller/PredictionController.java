package com.flightspredictor.flights.controller;

import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.service.prediction.PredictionService;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/predict")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<ModelPredictionResponse> predict(@RequestBody @Valid PredictionRequest request) {
        String correlationId = MDC.get("correlationId");
        int requestHash = Objects.hash(
                request.flightDateTime().toLocalDateTime(),
                request.opUniqueCarrier(),
                request.origin(),
                request.dest(),
                null
        );
        log.info(
                "PREDICT_CONTROLLER_ENTER correlationId={} requestHash={} thread={}",
                correlationId,
                requestHash,
                Thread.currentThread().getName()
        );

        return ResponseEntity.ok(predictionService.predict(request));
    }
}
