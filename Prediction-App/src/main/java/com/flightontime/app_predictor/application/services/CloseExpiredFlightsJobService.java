package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CloseExpiredFlightsJobService {
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;

    public CloseExpiredFlightsJobService(FlightRequestRepositoryPort flightRequestRepositoryPort) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
    }

    public void closeExpiredFlights() {
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime cutoff = nowUtc.minusDays(1);
        List<FlightRequest> expiredRequests = flightRequestRepositoryPort
                .findByFlightDateBeforeAndActive(cutoff);
        for (FlightRequest request : expiredRequests) {
            if (request == null || !request.active()) {
                continue;
            }
            FlightRequest closedRequest = new FlightRequest(
                    request.id(),
                    request.userId(),
                    request.flightDateUtc(),
                    request.airlineCode(),
                    request.originIata(),
                    request.destIata(),
                    request.distance(),
                    request.flightNumber(),
                    request.createdAt(),
                    false,
                    nowUtc
            );
            flightRequestRepositoryPort.save(closedRequest);
        }
    }
}
