package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.UserPredictionSource;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class T12hNotifyJobServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private FlightFollowRepositoryPort flightFollowRepositoryPort;

    @Mock
    private FlightRequestRepositoryPort flightRequestRepositoryPort;

    @Mock
    private UserPredictionRepositoryPort userPredictionRepositoryPort;

    @Mock
    private PredictionRepositoryPort predictionRepositoryPort;

    @Mock
    private ModelPredictionPort modelPredictionPort;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private NotificationLogRepositoryPort notificationLogRepositoryPort;

    @Mock
    private DistanceUseCase distanceUseCase;

    private T12hNotifyJobService service;

    @BeforeEach
    void setUp() {
        service = new T12hNotifyJobService(
                flightFollowRepositoryPort,
                flightRequestRepositoryPort,
                userPredictionRepositoryPort,
                predictionRepositoryPort,
                modelPredictionPort,
                notificationPort,
                notificationLogRepositoryPort,
                distanceUseCase,
                FIXED_CLOCK
        );
    }

    @Test
    void givenMultipleChangesForUser_whenNotify_thenSendsSingleSummaryAndLogs() {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);
        FlightFollow follow1 = new FlightFollow(1L, 42L, 100L, RefreshMode.T12_ONLY, 10L, now, now);
        FlightFollow follow2 = new FlightFollow(2L, 42L, 200L, RefreshMode.T12_ONLY, 20L, now, now);
        FlightRequest request1 = baseRequest(100L, now.plusHours(12));
        FlightRequest request2 = baseRequest(200L, now.plusHours(12));

        when(flightFollowRepositoryPort.findByFlightDateBetween(any(), any()))
                .thenReturn(List.of(follow1, follow2));
        when(flightRequestRepositoryPort.findById(100L)).thenReturn(Optional.of(request1));
        when(flightRequestRepositoryPort.findById(200L)).thenReturn(Optional.of(request2));
        when(notificationLogRepositoryPort.findByUserIdAndRequestIdAndType(eq(42L), eq(100L), eq("T12H")))
                .thenReturn(Optional.empty());
        when(notificationLogRepositoryPort.findByUserIdAndRequestIdAndType(eq(42L), eq(200L), eq("T12H")))
                .thenReturn(Optional.empty());
        when(userPredictionRepositoryPort.findById(10L))
                .thenReturn(Optional.of(baselineSnapshot(10L, 42L, 100L, 1000L)));
        when(userPredictionRepositoryPort.findById(20L))
                .thenReturn(Optional.of(baselineSnapshot(20L, 42L, 200L, 2000L)));
        when(predictionRepositoryPort.findById(1000L))
                .thenReturn(Optional.of(baselinePrediction(1000L, 100L, "ON_TIME")));
        when(predictionRepositoryPort.findById(2000L))
                .thenReturn(Optional.of(baselinePrediction(2000L, 200L, "ON_TIME")));
        when(predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(eq(100L), any()))
                .thenReturn(Optional.empty());
        when(predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(eq(200L), any()))
                .thenReturn(Optional.empty());
        when(modelPredictionPort.requestPrediction(any()))
                .thenReturn(new ModelPrediction("DELAYED", 0.8, "HIGH", 0.6, "v1"));
        when(predictionRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyUsers();

        ArgumentCaptor<List<String>> messageCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationPort).sendT12hSummary(eq(42L), messageCaptor.capture());
        assertEquals(2, messageCaptor.getValue().size());
        verify(notificationLogRepositoryPort, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void givenExistingLog_whenNotify_thenSkipsNotification() {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);
        FlightFollow follow = new FlightFollow(1L, 42L, 100L, RefreshMode.T12_ONLY, 10L, now, now);
        FlightRequest request = baseRequest(100L, now.plusHours(12));

        when(flightFollowRepositoryPort.findByFlightDateBetween(any(), any()))
                .thenReturn(List.of(follow));
        when(flightRequestRepositoryPort.findById(100L)).thenReturn(Optional.of(request));
        when(notificationLogRepositoryPort.findByUserIdAndRequestIdAndType(eq(42L), eq(100L), eq("T12H")))
                .thenReturn(Optional.of(new NotificationLog(
                        1L,
                        42L,
                        100L,
                        "T12H",
                        10L,
                        "SYSTEM",
                        "ON_TIME",
                        "already sent",
                        now,
                        now
                )));

        service.notifyUsers();

        verify(notificationPort, never()).sendT12hSummary(any(), any());
        verify(modelPredictionPort, never()).requestPrediction(any());
        verify(notificationLogRepositoryPort, never()).save(any(NotificationLog.class));
    }

    private FlightRequest baseRequest(Long id, OffsetDateTime flightDateUtc) {
        return new FlightRequest(
                id,
                42L,
                flightDateUtc,
                "AA",
                "EZE",
                "JFK",
                8500.0,
                "102",
                flightDateUtc.minusDays(1),
                true,
                null
        );
    }

    private UserPrediction baselineSnapshot(Long snapshotId, Long userId, Long requestId, Long predictionId) {
        return new UserPrediction(
                snapshotId,
                userId,
                requestId,
                predictionId,
                UserPredictionSource.USER_QUERY,
                OffsetDateTime.now(FIXED_CLOCK)
        );
    }

    private Prediction baselinePrediction(Long predictionId, Long requestId, String status) {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);
        return new Prediction(
                predictionId,
                requestId,
                now.minusHours(3),
                status,
                0.9,
                "HIGH",
                0.7,
                "v1",
                PredictionSource.SYSTEM,
                now.minusHours(3),
                now.minusHours(3)
        );
    }
}
