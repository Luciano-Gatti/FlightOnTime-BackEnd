package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.domain.dto.prediction.PredictionStatsResponse;
import com.flightspredictor.flights.domain.enum.Prevision;
import com.flightspredictor.flights.domain.enum.Status;
import com.flightspredictor.flights.domain.entities.Prediction;
import com.flightspredictor.flights.domain.repository.PredictionRepository;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PredictionStatsService {

    private final PredictionRepository predictionRepository;

    public PredictionStatsService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public PredictionStatsResponse getStats() {
        List<Prediction> predictions = predictionRepository.findAll();

        Map<Status, Long> byStatus = predictions.stream()
                .collect(Collectors.groupingBy(Prediction::getStatus, Collectors.counting()));

        Map<Prevision, Long> byPrevision = predictions.stream()
                .collect(Collectors.groupingBy(Prediction::getPrevision, Collectors.counting()));

        DoubleSummaryStatistics probabilityStats = predictions.stream()
                .map(Prediction::getProbability)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        Double averageProbability = probabilityStats.getCount() > 0
                ? probabilityStats.getAverage()
                : null;

        return new PredictionStatsResponse(
                predictions.size(),
                byStatus,
                byPrevision,
                averageProbability
        );
    }
}
