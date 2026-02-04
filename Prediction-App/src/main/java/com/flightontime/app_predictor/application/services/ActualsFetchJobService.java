package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase ActualsFetchJobService.
 */
@Service
public class ActualsFetchJobService {
    private static final Logger log = LoggerFactory.getLogger(ActualsFetchJobService.class);

    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final FlightActualRepositoryPort flightActualRepositoryPort;
    private final FlightActualPort flightActualPort;

    /**
     * Ejecuta la operación actuals fetch job service.
     * @param flightRequestRepositoryPort variable de entrada flightRequestRepositoryPort.
     * @param flightActualRepositoryPort variable de entrada flightActualRepositoryPort.
     * @param flightActualPort variable de entrada flightActualPort.
     */

    /**
     * Ejecuta la operación actuals fetch job service.
     * @param flightRequestRepositoryPort variable de entrada flightRequestRepositoryPort.
     * @param flightActualRepositoryPort variable de entrada flightActualRepositoryPort.
     * @param flightActualPort variable de entrada flightActualPort.
     * @return resultado de la operación actuals fetch job service.
     */

    public ActualsFetchJobService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightActualRepositoryPort flightActualRepositoryPort,
            FlightActualPort flightActualPort
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightActualRepositoryPort = flightActualRepositoryPort;
        this.flightActualPort = flightActualPort;
    }

    /**
     * Ejecuta la operación fetch actuals.
     */

    public void fetchActuals() {
        fetchActuals(OffsetDateTime.now(ZoneOffset.UTC));
    }

    void fetchActuals(OffsetDateTime nowUtc) {
        long startMillis = System.currentTimeMillis();
        OffsetDateTime cutoffUtc = nowUtc.minusHours(12);
        log.info("Starting actuals fetch job timestamp={} cutoffUtc={}", nowUtc, cutoffUtc);
        List<FlightRequest> requests = flightRequestRepositoryPort
                .findActiveRequestsWithFlightDateUtcBefore(cutoffUtc);
        int flightsConsidered = requests.size();
        int flightsProcessed = 0;
        int actualsSaved = 0;
        int closedRequests = 0;
        int errors = 0;
        for (FlightRequest request : requests) {
            try {
                if (request == null) {
                    errors++;
                    continue;
                }
                ProcessResult result = processRequest(request, nowUtc);
                flightsProcessed++;
                actualsSaved += result.actualsSaved();
                closedRequests += result.closedRequests();
            } catch (Exception ex) {
                errors++;
                log.error("Error fetching actuals flight_request_id={}",
                        request != null ? request.id() : null, ex);
            }
        }
        long durationMs = System.currentTimeMillis() - startMillis;
        log.info("Finished actuals fetch job timestamp={} durationMs={} flightsConsidered={} flightsProcessed={} "
                        + "actualsSaved={} closedRequests={} errors={}",
                OffsetDateTime.now(ZoneOffset.UTC),
                durationMs,
                flightsConsidered,
                flightsProcessed,
                actualsSaved,
                closedRequests,
                errors);
    }

    /**
     * Ejecuta la operación process request.
     * @param request variable de entrada request.
     * @param nowUtc variable de entrada nowUtc.
     * @return resultado de la operación process request.
     */

    private ProcessResult processRequest(FlightRequest request, OffsetDateTime nowUtc) {
        if (!isExpired(request, nowUtc)) {
            return new ProcessResult(0, 0);
        }
        Optional<FlightActualResult> actualResult = fetchActualResult(request);
        if (actualResult.isEmpty()) {
            /**
             * Ejecuta la operación close if expired.
             * @param request variable de entrada request.
             * @param nowUtc variable de entrada nowUtc.
             * @return resultado de la operación close if expired.
             */
            return closeIfExpired(request, nowUtc);
        }
        FlightActualResult result = actualResult.get();
        verifyPrediction(request, result, nowUtc);
        if (!isPersistableStatus(result.actualStatus())) {
            /**
             * Ejecuta la operación close if expired.
             * @param request variable de entrada request.
             * @param nowUtc variable de entrada nowUtc.
             * @return resultado de la operación close if expired.
             */
            return closeIfExpired(request, nowUtc);
        }
        FlightActual actual = new FlightActual(
                null,
                request.id(),
                toUtc(request.flightDateUtc()),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                toUtc(result.actualDeparture()),
                toUtc(result.actualArrival()),
                result.actualStatus(),
                nowUtc
        );
        flightActualRepositoryPort.upsertByFlightRequestId(actual);
        ProcessResult closeResult = closeIfExpired(request, nowUtc);
        return new ProcessResult(1, closeResult.closedRequests());
    }

    /**
     * Ejecuta la operación fetch actual result.
     * @param request variable de entrada request.
     * @return resultado de la operación fetch actual result.
     */

    private Optional<FlightActualResult> fetchActualResult(FlightRequest request) {
        if (request == null || request.flightDateUtc() == null) {
            return Optional.empty();
        }
        if (hasFlightNumber(request.flightNumber())) {
            Optional<FlightActualResult> byNumber = flightActualPort
                    .fetchByFlightNumber(request.flightNumber(), toUtc(request.flightDateUtc()));
            if (byNumber.isPresent()) {
                return byNumber;
            }
        }
        OffsetDateTime flightDateUtc = toUtc(request.flightDateUtc());
        OffsetDateTime windowStart = flightDateUtc.minusHours(3);
        OffsetDateTime windowEnd = flightDateUtc.plusHours(3);
        return flightActualPort.fetchByRouteAndWindow(
                request.originIata(),
                request.destIata(),
                windowStart,
                windowEnd
        );
    }

    /**
     * Ejecuta la operación resolve yesterday window start.
     * @return resultado de la operación resolve yesterday window start.
     */

    /**
     * Ejecuta la operación has flight number.
     * @param flightNumber variable de entrada flightNumber.
     * @return resultado de la operación has flight number.
     */

    private boolean hasFlightNumber(String flightNumber) {
        return flightNumber != null && !flightNumber.isBlank();
    }

    /**
     * Ejecuta la operación is persistable status.
     * @param status variable de entrada status.
     * @return resultado de la operación is persistable status.
     */

    private boolean isPersistableStatus(String status) {
        return "ON_TIME".equals(status) || "DELAYED".equals(status) || "CANCELLED".equals(status);
    }

    /**
     * Ejecuta la operación close if expired.
     * @param request variable de entrada request.
     * @param nowUtc variable de entrada nowUtc.
     * @return resultado de la operación close if expired.
     */

    private ProcessResult closeIfExpired(FlightRequest request, OffsetDateTime nowUtc) {
        if (request == null || request.flightDateUtc() == null || !request.active()) {
            return new ProcessResult(0, 0);
        }
        if (isExpired(request, nowUtc)) {
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
            return new ProcessResult(0, 1);
        }
        return new ProcessResult(0, 0);
    }

    private boolean isExpired(FlightRequest request, OffsetDateTime nowUtc) {
        if (request == null || request.flightDateUtc() == null) {
            return false;
        }
        OffsetDateTime flightDateUtc = toUtc(request.flightDateUtc());
        return !flightDateUtc.plusHours(12).isAfter(nowUtc);
    }

    private void verifyPrediction(FlightRequest request, FlightActualResult actualResult, OffsetDateTime nowUtc) {
        // TODO: integrar componente de verificación de predicción cuando esté disponible.
    }

    /**
     * Ejecuta la operación to utc.
     * @param value variable de entrada value.
     * @return resultado de la operación to utc.
     */

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    /**
     * Ejecuta la operación process result.
     * @param actualsSaved variable de entrada actualsSaved.
     * @param closedRequests variable de entrada closedRequests.
     * @return resultado de la operación process result.
     */

    /**
     * Record ProcessResult.
     *
     * <p>Responsable de process result.</p>
     * @param actualsSaved variable de entrada actualsSaved.
     * @param closedRequests variable de entrada closedRequests.
     * @return resultado de la operación resultado.
     */

    private record ProcessResult(int actualsSaved, int closedRequests) {
    }
}
