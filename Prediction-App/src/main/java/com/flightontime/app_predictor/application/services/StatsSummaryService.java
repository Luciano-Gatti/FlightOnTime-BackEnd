package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import com.flightontime.app_predictor.domain.ports.in.StatsSummaryUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsSummaryResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.StatsTopFlightDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

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
    public StatsSummaryResponseDTO getSummary(int topN) {
        long totalPredictions = predictionRepositoryPort.countAll();
        long totalOnTimePredicted = predictionRepositoryPort.countByStatus("ON_TIME");
        long totalDelayedPredicted = predictionRepositoryPort.countByStatus("DELAYED");
        long totalFlightsWithActual = flightActualRepositoryPort.countAll();
        long totalCancelled = flightActualRepositoryPort.countByStatus("CANCELLED");
        List<StatsTopFlightDTO> topFlights = resolveTopFlights(topN);
        return new StatsSummaryResponseDTO(
                totalPredictions,
                totalOnTimePredicted,
                totalDelayedPredicted,
                totalFlightsWithActual,
                totalCancelled,
                topFlights
        );
    }

    private List<StatsTopFlightDTO> resolveTopFlights(int topN) {
        if (topN <= 0) {
            return Collections.emptyList();
        }
        List<FlightRequestPopularity> popularity = userPredictionRepositoryPort.findTopRequestPopularity(topN);
        if (popularity.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = popularity.stream()
                .map(FlightRequestPopularity::requestId)
                .collect(Collectors.toList());
        List<FlightRequest> requests = flightRequestRepositoryPort.findByIds(requestIds);
        Map<Long, FlightRequest> requestMap = new HashMap<>();
        for (FlightRequest request : requests) {
            requestMap.put(request.id(), request);
        }
        return popularity.stream()
                .map(item -> toTopFlight(item, requestMap.get(item.requestId())))
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    private StatsTopFlightDTO toTopFlight(FlightRequestPopularity popularity, FlightRequest request) {
        if (request == null) {
            return null;
        }
        return new StatsTopFlightDTO(
                request.id(),
                toUtc(request.flightDate()),
                request.carrier(),
                request.origin(),
                request.destination(),
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
