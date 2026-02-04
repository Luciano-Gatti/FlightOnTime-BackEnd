package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightActual;
import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    private static final ZoneId EXECUTION_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

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
        long startMillis = System.currentTimeMillis();
        OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime windowStart = resolveYesterdayWindowStart();
        OffsetDateTime windowEnd = resolveYesterdayWindowEnd();
        log.info("Starting actuals fetch job timestamp={} windowStart={} windowEnd={}",
                startTimestamp, windowStart, windowEnd);
        List<FlightRequest> requests = flightRequestRepositoryPort
                .findByFlightDateBetweenWithoutActuals(windowStart, windowEnd);
        int flightsInWindow = requests.size();
        int actualsSaved = 0;
        int closedRequests = 0;
        int errors = 0;
        for (FlightRequest request : requests) {
            try {
                ProcessResult result = processRequest(request, startTimestamp);
                actualsSaved += result.actualsSaved();
                closedRequests += result.closedRequests();
            } catch (Exception ex) {
                errors++;
                log.error("Error fetching actuals flight_request_id={}",
                        request != null ? request.id() : null, ex);
            }
        }
        long durationMs = System.currentTimeMillis() - startMillis;
        log.info("Finished actuals fetch job timestamp={} durationMs={} flightsInWindow={} actualsSaved={} "
                        + "closedRequests={} errors={}",
                OffsetDateTime.now(ZoneOffset.UTC),
                durationMs,
                flightsInWindow,
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
        flightActualRepositoryPort.save(actual);
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

    private OffsetDateTime resolveYesterdayWindowStart() {
        LocalDate yesterday = LocalDate.now(EXECUTION_ZONE).minusDays(1);
        return yesterday.atStartOfDay(EXECUTION_ZONE).toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

    /**
     * Ejecuta la operación resolve yesterday window end.
     * @return resultado de la operación resolve yesterday window end.
     */

    private OffsetDateTime resolveYesterdayWindowEnd() {
        LocalDate yesterday = LocalDate.now(EXECUTION_ZONE).minusDays(1);
        return yesterday.atTime(23, 59, 59).atZone(EXECUTION_ZONE).toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

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
        if (request.flightDateUtc().isBefore(nowUtc)) {
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
