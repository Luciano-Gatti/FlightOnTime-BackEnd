package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import com.flightontime.app_predictor.domain.model.StatsSummary;
import com.flightontime.app_predictor.domain.model.StatsTopFlight;
import com.flightontime.app_predictor.domain.ports.in.StatsSummaryUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Clase StatsSummaryService.
 */
@Service
public class StatsSummaryService implements StatsSummaryUseCase {
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final FlightActualRepositoryPort flightActualRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;

    public StatsSummaryService(
            PredictionRepositoryPort predictionRepositoryPort,
            FlightActualRepositoryPort flightActualRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            FlightRequestRepositoryPort flightRequestRepositoryPort
    ) {
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.flightActualRepositoryPort = flightActualRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
    }

    @Override
    public StatsSummary getSummary(int topN) {
        long totalPredictions = predictionRepositoryPort.countAll();
        long totalOnTimePredicted = predictionRepositoryPort.countByStatus("ON_TIME");
        long totalDelayedPredicted = predictionRepositoryPort.countByStatus("DELAYED");
        long totalFlightsWithActual = flightActualRepositoryPort.countAll();
        long totalCancelled = flightActualRepositoryPort.countByActualStatus("CANCELLED");
        List<StatsTopFlight> topFlights = resolveTopFlights(topN);
        return new StatsSummary(
                totalPredictions,
                totalOnTimePredicted,
                totalDelayedPredicted,
                totalFlightsWithActual,
                totalCancelled,
                topFlights
        );
    }

    private List<StatsTopFlight> resolveTopFlights(int topN) {
        if (topN <= 0) {
            return Collections.emptyList();
        }
        List<FlightRequestPopularity> popularity = userPredictionRepositoryPort.findTopRequestPopularity(topN);
        if (popularity.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = popularity.stream()
                .map(FlightRequestPopularity::flightRequestId)
                .collect(Collectors.toList());
        List<FlightRequest> requests = flightRequestRepositoryPort.findByIds(requestIds);
        Map<Long, FlightRequest> requestMap = new HashMap<>();
        for (FlightRequest request : requests) {
            requestMap.put(request.id(), request);
        }
        return popularity.stream()
                .map(item -> toTopFlight(item, requestMap.get(item.flightRequestId())))
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    private StatsTopFlight toTopFlight(FlightRequestPopularity popularity, FlightRequest request) {
        if (request == null) {
            return null;
        }
        return new StatsTopFlight(
                request.id(),
                toUtc(request.flightDateUtc()),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                popularity.uniqueUsers()
        );
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
