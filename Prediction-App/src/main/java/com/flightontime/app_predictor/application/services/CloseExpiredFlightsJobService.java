package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase CloseExpiredFlightsJobService.
 */
@Service
public class CloseExpiredFlightsJobService {
    private static final Logger log = LoggerFactory.getLogger(CloseExpiredFlightsJobService.class);
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;

    public CloseExpiredFlightsJobService(FlightRequestRepositoryPort flightRequestRepositoryPort) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
    }

    public void closeExpiredFlights() {
        long startMillis = System.currentTimeMillis();
        OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime cutoff = startTimestamp.minusDays(1);
        log.info("Starting close expired flights job timestamp={} cutoff={}", startTimestamp, cutoff);
        List<FlightRequest> expiredRequests = flightRequestRepositoryPort
                .findByFlightDateBeforeAndActive(cutoff);
        int subscriptionsEvaluated = expiredRequests.size();
        int closedRequests = 0;
        int errors = 0;
        for (FlightRequest request : expiredRequests) {
            try {
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
                        startTimestamp
                );
                flightRequestRepositoryPort.save(closedRequest);
                closedRequests++;
            } catch (Exception ex) {
                errors++;
                log.error("Error closing expired flight flight_request_id={} user_id={}",
                        request != null ? request.id() : null,
                        request != null ? request.userId() : null,
                        ex);
            }
        }
        long durationMs = System.currentTimeMillis() - startMillis;
        log.info("Finished close expired flights job timestamp={} durationMs={} subscriptionsEvaluated={} "
                        + "closedRequests={} errors={}",
                OffsetDateTime.now(ZoneOffset.UTC),
                durationMs,
                subscriptionsEvaluated,
                closedRequests,
                errors);
    }
}
