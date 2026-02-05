package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.StatsAccuracyBin;
import com.flightontime.app_predictor.domain.model.StatsAccuracyByLeadTime;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightOutcomeEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightRequestEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightOutcomeJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightPredictionJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightRequestJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatsAccuracyByLeadTimeServiceTest {

    @Autowired
    private StatsAccuracyByLeadTimeService statsAccuracyByLeadTimeService;

    @Autowired
    private FlightRequestJpaRepository flightRequestJpaRepository;

    @Autowired
    private FlightPredictionJpaRepository flightPredictionJpaRepository;

    @Autowired
    private FlightOutcomeJpaRepository flightOutcomeJpaRepository;

    @BeforeEach
    void setUp() {
        flightOutcomeJpaRepository.deleteAll();
        flightPredictionJpaRepository.deleteAll();
        flightRequestJpaRepository.deleteAll();
    }

    @Test
    void givenOutcomes_whenGetAccuracyByLeadTime_thenExcludesCancelledAndBins() {
        FlightRequestEntity request1 = createRequest(OffsetDateTime.parse("2025-01-02T00:00:00Z"));
        FlightRequestEntity request2 = createRequest(OffsetDateTime.parse("2025-01-02T06:00:00Z"));
        FlightRequestEntity cancelledRequest = createRequest(OffsetDateTime.parse("2025-01-03T00:00:00Z"));

        flightPredictionJpaRepository.save(createPrediction(
                request1.getId(),
                OffsetDateTime.parse("2025-01-01T19:00:00Z"),
                "ON_TIME"
        ));
        flightPredictionJpaRepository.save(createPrediction(
                request2.getId(),
                OffsetDateTime.parse("2025-01-01T04:00:00Z"),
                "ON_TIME"
        ));
        flightPredictionJpaRepository.save(createPrediction(
                cancelledRequest.getId(),
                OffsetDateTime.parse("2025-01-02T00:00:00Z"),
                "ON_TIME"
        ));

        flightOutcomeJpaRepository.save(createOutcome(request1, "ON_TIME"));
        flightOutcomeJpaRepository.save(createOutcome(request2, "DELAYED"));
        flightOutcomeJpaRepository.save(createOutcome(cancelledRequest, "CANCELLED"));

        StatsAccuracyByLeadTime result = statsAccuracyByLeadTimeService.getAccuracyByLeadTime();
        assertNotNull(result);

        StatsAccuracyBin bin3to6 = findBin(result.bins(), "3-6");
        StatsAccuracyBin bin24to27 = findBin(result.bins(), "24-27");

        assertEquals(1, bin3to6.total());
        assertEquals(1, bin3to6.correct());
        assertEquals(1.0, bin3to6.accuracy());

        assertEquals(1, bin24to27.total());
        assertEquals(0, bin24to27.correct());
        assertEquals(0.0, bin24to27.accuracy());
    }

    private FlightRequestEntity createRequest(OffsetDateTime flightDateUtc) {
        FlightRequestEntity request = new FlightRequestEntity();
        request.setFlightDateUtc(flightDateUtc);
        request.setAirlineCode("AA");
        request.setOriginIata("EZE");
        request.setDestIata("JFK");
        request.setDistance(8500.0);
        request.setFlightNumber("102");
        request.setActive(true);
        return flightRequestJpaRepository.save(request);
    }

    private FlightPredictionEntity createPrediction(
            Long requestId,
            OffsetDateTime forecastBucketUtc,
            String predictedStatus
    ) {
        FlightPredictionEntity prediction = new FlightPredictionEntity();
        prediction.setFlightRequestId(requestId);
        prediction.setForecastBucketUtc(forecastBucketUtc);
        prediction.setPredictedStatus(predictedStatus);
        prediction.setPredictedProbability(0.8);
        prediction.setConfidence("HIGH");
        prediction.setThresholdUsed(0.7);
        prediction.setModelVersion("v1");
        prediction.setSource(com.flightontime.app_predictor.domain.model.PredictionSource.SYSTEM);
        prediction.setPredictedAt(forecastBucketUtc);
        prediction.setCreatedAt(forecastBucketUtc);
        return prediction;
    }

    private FlightOutcomeEntity createOutcome(FlightRequestEntity request, String actualStatus) {
        FlightOutcomeEntity outcome = new FlightOutcomeEntity();
        outcome.setFlightRequestId(request.getId());
        outcome.setFlightDateUtc(request.getFlightDateUtc());
        outcome.setAirlineCode(request.getAirlineCode());
        outcome.setOriginIata(request.getOriginIata());
        outcome.setDestIata(request.getDestIata());
        outcome.setFlightNumber(request.getFlightNumber());
        outcome.setActualStatus(actualStatus);
        return outcome;
    }

    private StatsAccuracyBin findBin(List<StatsAccuracyBin> bins, String label) {
        return bins.stream()
                .filter(bin -> label.equals(bin.leadTimeHours()))
                .findFirst()
                .orElseThrow();
    }
}
