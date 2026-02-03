package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import com.flightontime.app_predictor.domain.ports.in.StatsAccuracyByLeadTimeUseCase;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyBinDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsAccuracyByLeadTimeResponseDTO;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatsAccuracyByLeadTimeService implements StatsAccuracyByLeadTimeUseCase {
    private static final int BIN_SIZE_HOURS = 3;
    private static final int MAX_BIN_EDGE_HOURS = 72;

    private final PredictionRepositoryPort predictionRepositoryPort;

    public StatsAccuracyByLeadTimeService(PredictionRepositoryPort predictionRepositoryPort) {
        this.predictionRepositoryPort = predictionRepositoryPort;
    }

    @Override
    public StatsAccuracyByLeadTimeResponseDTO getAccuracyByLeadTime() {
        List<PredictionAccuracySample> samples = predictionRepositoryPort.findAccuracySamplesExcludingCancelled();
        List<BinAccumulator> bins = initializeBins();
        for (PredictionAccuracySample sample : samples) {
            int hours = computeLeadTimeHours(sample.forecastBucketUtc(), sample.flightDateUtc());
            int index = resolveBinIndex(hours);
            BinAccumulator bin = bins.get(index);
            bin.total++;
            if (isCorrect(sample.predictedStatus(), sample.actualStatus())) {
                bin.correct++;
            }
        }
        List<StatsAccuracyBinDTO> responseBins = new ArrayList<>();
        for (BinAccumulator bin : bins) {
            responseBins.add(new StatsAccuracyBinDTO(
                    bin.label,
                    bin.total,
                    bin.correct,
                    bin.total == 0 ? 0.0 : (double) bin.correct / bin.total
            ));
        }
        return new StatsAccuracyByLeadTimeResponseDTO(responseBins);
    }

    private List<BinAccumulator> initializeBins() {
        List<BinAccumulator> bins = new ArrayList<>();
        for (int start = 0; start < MAX_BIN_EDGE_HOURS; start += BIN_SIZE_HOURS) {
            int end = start + BIN_SIZE_HOURS;
            bins.add(new BinAccumulator(start + "-" + end));
        }
        bins.add(new BinAccumulator(MAX_BIN_EDGE_HOURS + "+"));
        return bins;
    }

    private int computeLeadTimeHours(OffsetDateTime predictedAt, OffsetDateTime flightDate) {
        if (predictedAt == null || flightDate == null) {
            return 0;
        }
        OffsetDateTime predictedUtc = predictedAt.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime flightUtc = flightDate.withOffsetSameInstant(ZoneOffset.UTC);
        long hours = Duration.between(predictedUtc, flightUtc).toHours();
        if (hours < 0) {
            return 0;
        }
        if (hours > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) hours;
    }

    private int resolveBinIndex(int hours) {
        if (hours >= MAX_BIN_EDGE_HOURS) {
            return MAX_BIN_EDGE_HOURS / BIN_SIZE_HOURS;
        }
        return hours / BIN_SIZE_HOURS;
    }

    private boolean isCorrect(String predictedStatus, String actualStatus) {
        if (predictedStatus == null || actualStatus == null) {
            return false;
        }
        return predictedStatus.equals(actualStatus);
    }

    private static class BinAccumulator {
        private final String label;
        private long total;
        private long correct;

        private BinAccumulator(String label) {
            this.label = label;
        }
    }
}
