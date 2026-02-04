package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class T72hRefreshJobServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private FlightFollowRepositoryPort flightFollowRepositoryPort;

    @Mock
    private FlightRequestRepositoryPort flightRequestRepositoryPort;

    @Mock
    private PredictionRepositoryPort predictionRepositoryPort;

    @Mock
    private ModelPredictionPort modelPredictionPort;

    @Mock
    private DistanceUseCase distanceUseCase;

    private T72hRefreshJobService service;

    @BeforeEach
    void setUp() {
        service = new T72hRefreshJobService(
                flightFollowRepositoryPort,
                flightRequestRepositoryPort,
                predictionRepositoryPort,
                modelPredictionPort,
                distanceUseCase,
                FIXED_CLOCK
        );
    }

    @Test
    void givenMissingBucket_whenRefreshPredictions_thenCreatesPrediction() {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);
        FlightFollow follow = new FlightFollow(1L, 10L, 100L, RefreshMode.T72_REFRESH, null, now, now);
        FlightRequest request = new FlightRequest(
                100L,
                10L,
                now.plusHours(10),
                "AA",
                "EZE",
                "JFK",
                8500.0,
                "102",
                now.minusDays(1),
                true,
                null
        );

        when(flightFollowRepositoryPort.findByRefreshModeAndFlightDateBetween(
                eq(RefreshMode.T72_REFRESH),
                any(),
                any()
        )).thenReturn(List.of(follow));
        when(flightRequestRepositoryPort.findById(100L)).thenReturn(Optional.of(request));
        when(predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(modelPredictionPort.requestPrediction(any()))
                .thenReturn(new ModelPrediction("ON_TIME", 0.9, "HIGH", 0.7, "v1"));
        when(predictionRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.refreshPredictions();

        verify(modelPredictionPort).requestPrediction(any());
        verify(predictionRepositoryPort).save(any(Prediction.class));
    }

    @Test
    void givenCachedBucket_whenRefreshPredictions_thenSkipsModelCall() {
        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);
        FlightFollow follow = new FlightFollow(1L, 10L, 100L, RefreshMode.T72_REFRESH, null, now, now);
        FlightRequest request = new FlightRequest(
                100L,
                10L,
                now.plusHours(10),
                "AA",
                "EZE",
                "JFK",
                8500.0,
                "102",
                now.minusDays(1),
                true,
                null
        );
        Prediction cached = new Prediction(
                55L,
                100L,
                now,
                "ON_TIME",
                0.9,
                "HIGH",
                0.7,
                "v1",
                PredictionSource.SYSTEM,
                now,
                now
        );

        when(flightFollowRepositoryPort.findByRefreshModeAndFlightDateBetween(
                eq(RefreshMode.T72_REFRESH),
                any(),
                any()
        )).thenReturn(List.of(follow));
        when(flightRequestRepositoryPort.findById(100L)).thenReturn(Optional.of(request));
        when(predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(anyLong(), any()))
                .thenReturn(Optional.of(cached));

        service.refreshPredictions();

        verify(modelPredictionPort, never()).requestPrediction(any());
        verify(predictionRepositoryPort, never()).save(any(Prediction.class));
    }
}
