package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.domain.dto.prediction.PredictionStatsResponse;
import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.repository.FlightPredictionRepository;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PredictionStatsService {

    private final FlightPredictionRepository predictionRepository;

    public PredictionStatsService(FlightPredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public PredictionStatsResponse getStats() {
        List<FlightPrediction> predictions = predictionRepository.findAll();

        Map<PredictedStatus, Long> byStatus = predictions.stream()
                .collect(Collectors.groupingBy(FlightPrediction::getPredictedStatus, Collectors.counting()));

        Map<String, Long> byConfidence = predictions.stream()
                .collect(Collectors.groupingBy(FlightPrediction::getConfidence, Collectors.counting()));

        DoubleSummaryStatistics probabilityStats = predictions.stream()
                .map(FlightPrediction::getPredictedProbability)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        Double averageProbability = probabilityStats.getCount() > 0
                ? probabilityStats.getAverage()
                : null;

        return new PredictionStatsResponse(
                predictions.size(),
                byStatus,
                byConfidence,
                averageProbability
        );
    }
}
