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
import org.springframework.stereotype.Service;

@Service
public class ActualsFetchJobService {
    private static final ZoneId EXECUTION_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final FlightActualRepositoryPort flightActualRepositoryPort;
    private final FlightActualPort flightActualPort;

    public ActualsFetchJobService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightActualRepositoryPort flightActualRepositoryPort,
            FlightActualPort flightActualPort
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightActualRepositoryPort = flightActualRepositoryPort;
        this.flightActualPort = flightActualPort;
    }

    public void fetchActuals() {
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime windowStart = resolveYesterdayWindowStart();
        OffsetDateTime windowEnd = resolveYesterdayWindowEnd();
        List<FlightRequest> requests = flightRequestRepositoryPort
                .findByFlightDateBetweenWithoutActuals(windowStart, windowEnd);
        for (FlightRequest request : requests) {
            processRequest(request, nowUtc);
        }
    }

    private void processRequest(FlightRequest request, OffsetDateTime nowUtc) {
        Optional<FlightActualResult> actualResult = fetchActualResult(request);
        if (actualResult.isEmpty()) {
            closeIfExpired(request, nowUtc);
            return;
        }
        FlightActualResult result = actualResult.get();
        if (!isPersistableStatus(result.status())) {
            closeIfExpired(request, nowUtc);
            return;
        }
        FlightActual actual = new FlightActual(
                null,
                request.id(),
                toUtc(request.flightDate()),
                request.carrier(),
                request.origin(),
                request.destination(),
                request.flightNumber(),
                toUtc(result.actualDeparture()),
                toUtc(result.actualArrival()),
                result.status(),
                nowUtc
        );
        flightActualRepositoryPort.save(actual);
        closeIfExpired(request, nowUtc);
    }

    private Optional<FlightActualResult> fetchActualResult(FlightRequest request) {
        if (request == null || request.flightDate() == null) {
            return Optional.empty();
        }
        if (hasFlightNumber(request.flightNumber())) {
            Optional<FlightActualResult> byNumber = flightActualPort
                    .fetchByFlightNumber(request.flightNumber(), toUtc(request.flightDate()));
            if (byNumber.isPresent()) {
                return byNumber;
            }
        }
        OffsetDateTime flightDateUtc = toUtc(request.flightDate());
        OffsetDateTime windowStart = flightDateUtc.minusHours(3);
        OffsetDateTime windowEnd = flightDateUtc.plusHours(3);
        return flightActualPort.fetchByRouteAndWindow(
                request.origin(),
                request.destination(),
                windowStart,
                windowEnd
        );
    }

    private OffsetDateTime resolveYesterdayWindowStart() {
        LocalDate yesterday = LocalDate.now(EXECUTION_ZONE).minusDays(1);
        return yesterday.atStartOfDay(EXECUTION_ZONE).toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

    private OffsetDateTime resolveYesterdayWindowEnd() {
        LocalDate yesterday = LocalDate.now(EXECUTION_ZONE).minusDays(1);
        return yesterday.atTime(23, 59, 59).atZone(EXECUTION_ZONE).toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

    private boolean hasFlightNumber(String flightNumber) {
        return flightNumber != null && !flightNumber.isBlank();
    }

    private boolean isPersistableStatus(String status) {
        return "ON_TIME".equals(status) || "DELAYED".equals(status) || "CANCELLED".equals(status);
    }

    private void closeIfExpired(FlightRequest request, OffsetDateTime nowUtc) {
        if (request == null || request.flightDate() == null || !request.active()) {
            return;
        }
        if (request.flightDate().isBefore(nowUtc)) {
            FlightRequest closedRequest = new FlightRequest(
                    request.id(),
                    request.userId(),
                    request.flightDate(),
                    request.carrier(),
                    request.origin(),
                    request.destination(),
                    request.flightNumber(),
                    request.createdAt(),
                    false,
                    nowUtc
            );
            flightRequestRepositoryPort.save(closedRequest);
        }
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
