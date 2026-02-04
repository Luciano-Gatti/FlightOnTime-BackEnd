package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightActualResult;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightActualPort;
import com.flightontime.app_predictor.domain.ports.out.FlightActualRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActualsFetchJobServiceTest {
    @Mock
    private FlightRequestRepositoryPort flightRequestRepositoryPort;

    @Mock
    private FlightActualRepositoryPort flightActualRepositoryPort;

    @Mock
    private FlightActualPort flightActualPort;

    @InjectMocks
    private ActualsFetchJobService actualsFetchJobService;

    @Captor
    private ArgumentCaptor<FlightRequest> requestCaptor;

    @Test
    void closesRequestWhenFlightIsExactlyTwelveHoursOld() {
        OffsetDateTime nowUtc = OffsetDateTime.of(2024, 1, 2, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime flightDateUtc = nowUtc.minusHours(12);
        FlightRequest request = buildRequest(flightDateUtc, null);

        when(flightRequestRepositoryPort.findActiveRequestsWithFlightDateUtcBefore(eq(nowUtc.minusHours(12))))
                .thenReturn(List.of(request));
        when(flightActualPort.fetchByRouteAndWindow(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        actualsFetchJobService.fetchActuals(nowUtc);

        verify(flightRequestRepositoryPort).save(requestCaptor.capture());
        FlightRequest closed = requestCaptor.getValue();
        assertThat(closed.active()).isFalse();
        assertThat(closed.closedAt()).isEqualTo(nowUtc);
        verify(flightActualRepositoryPort, never()).upsertByFlightRequestId(any());
    }

    @Test
    void fallsBackToRouteSearchWhenFlightNumberHasNoResult() {
        OffsetDateTime nowUtc = OffsetDateTime.of(2024, 1, 2, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime flightDateUtc = nowUtc.minusHours(13);
        FlightRequest request = buildRequest(flightDateUtc, "AR123");
        FlightActualResult actualResult = new FlightActualResult(
                "ON_TIME",
                flightDateUtc.plusMinutes(15),
                flightDateUtc.plusHours(2)
        );

        when(flightRequestRepositoryPort.findActiveRequestsWithFlightDateUtcBefore(eq(nowUtc.minusHours(12))))
                .thenReturn(List.of(request));
        when(flightActualPort.fetchByFlightNumber(eq("AR123"), eq(flightDateUtc)))
                .thenReturn(Optional.empty());
        when(flightActualPort.fetchByRouteAndWindow(
                eq("EZE"),
                eq("MIA"),
                eq(flightDateUtc.minusHours(3)),
                eq(flightDateUtc.plusHours(3))
        )).thenReturn(Optional.of(actualResult));

        actualsFetchJobService.fetchActuals(nowUtc);

        verify(flightActualPort).fetchByRouteAndWindow(
                eq("EZE"),
                eq("MIA"),
                eq(flightDateUtc.minusHours(3)),
                eq(flightDateUtc.plusHours(3))
        );
        verify(flightActualRepositoryPort).upsertByFlightRequestId(any());
    }

    private FlightRequest buildRequest(OffsetDateTime flightDateUtc, String flightNumber) {
        return new FlightRequest(
                1L,
                99L,
                flightDateUtc,
                "AR",
                "EZE",
                "MIA",
                0d,
                flightNumber,
                flightDateUtc.minusDays(1),
                true,
                null
        );
    }
}
