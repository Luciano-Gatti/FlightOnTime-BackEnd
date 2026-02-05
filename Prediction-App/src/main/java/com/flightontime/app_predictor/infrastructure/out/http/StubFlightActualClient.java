package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub de actuals de vuelo para desarrollo local.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "true")
public class StubFlightActualClient implements FlightActualPort {

    @Override
    public Optional<FlightActualResult> fetchByFlightNumber(String flightNumber, OffsetDateTime flightDate) {
        if (flightDate == null) {
            return Optional.empty();
        }
        return Optional.of(buildResult(flightDate));
    }

    @Override
    public Optional<FlightActualResult> fetchByRouteAndWindow(
            String originIata,
            String destIata,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd
    ) {
        if (windowStart == null) {
            return Optional.empty();
        }
        return Optional.of(buildResult(windowStart));
    }

    private FlightActualResult buildResult(OffsetDateTime baseDate) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String status = baseDate.getDayOfMonth() % 2 == 0 ? "ON_TIME" : "DELAYED";
        return new FlightActualResult(status, now.minusMinutes(40), now);
    }
}
