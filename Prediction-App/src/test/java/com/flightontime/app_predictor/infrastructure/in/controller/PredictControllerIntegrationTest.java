package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.domain.model.ModelPrediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import com.flightontime.app_predictor.infrastructure.out.entities.AirportEntity;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightSubscriptionEntity;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionSnapshotEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.AirportJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightPredictionJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightRequestJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightSubscriptionJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.repository.UserPredictionSnapshotJpaRepository;
import com.flightontime.app_predictor.infrastructure.out.repository.UserJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PredictControllerIntegrationTest {

    private static final OffsetDateTime FLIGHT_DATE = OffsetDateTime.parse("2030-05-01T14:30:00Z");
    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.parse("2025-01-01T01:30:00Z");
    private static final OffsetDateTime BUCKET_START = OffsetDateTime.parse("2025-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AirportJpaRepository airportJpaRepository;

    @Autowired
    private FlightRequestJpaRepository flightRequestJpaRepository;

    @Autowired
    private FlightPredictionJpaRepository flightPredictionJpaRepository;

    @Autowired
    private UserPredictionSnapshotJpaRepository userPredictionSnapshotJpaRepository;

    @Autowired
    private FlightSubscriptionJpaRepository flightSubscriptionJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @MockBean
    private ModelPredictionPort modelPredictionPort;

    @MockBean
    private WeatherPort weatherPort;

    @MockBean
    private AirportInfoPort airportInfoPort;

    @MockBean
    private NotificationPort notificationPort;

    @BeforeEach
    void setUp() {
        flightSubscriptionJpaRepository.deleteAll();
        userPredictionSnapshotJpaRepository.deleteAll();
        flightPredictionJpaRepository.deleteAll();
        flightRequestJpaRepository.deleteAll();
        airportJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        seedAirports();
        seedUser();
        when(modelPredictionPort.requestPrediction(any()))
                .thenReturn(new ModelPrediction("ON_TIME", 0.9, "HIGH", 0.7, "v1"));
    }

    @Test
    @DisplayName("Anonymous user persists flight request and prediction without snapshots or subscriptions")
    void givenAnonymousUser_whenPredict_thenPersistsRequestAndPrediction() throws Exception {
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestPayload()))
                .andExpect(status().isOk());

        assertEquals(1, flightRequestJpaRepository.count());
        assertEquals(1, flightPredictionJpaRepository.count());
        assertEquals(0, userPredictionSnapshotJpaRepository.count());
        assertEquals(0, flightSubscriptionJpaRepository.count());
    }

    @Test
    @WithMockUser(username = "42", roles = "USER")
    @DisplayName("Authenticated user creates snapshot and updates subscription baseline and refresh mode")
    void givenAuthenticatedUser_whenPredict_thenCreatesSnapshotAndSubscription() throws Exception {
        FlightRequestEntity existingRequest = createExistingFlightRequest();
        FlightSubscriptionEntity existingFollow = new FlightSubscriptionEntity();
        existingFollow.setUserId(42L);
        existingFollow.setFlightRequestId(existingRequest.getId());
        existingFollow.setRefreshMode(RefreshMode.T12_ONLY);
        existingFollow.setBaselineSnapshotId(null);
        flightSubscriptionJpaRepository.save(existingFollow);

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestPayload()))
                .andExpect(status().isOk());

        assertEquals(1, userPredictionSnapshotJpaRepository.count());
        FlightSubscriptionEntity updatedFollow = flightSubscriptionJpaRepository
                .findFirstByUserIdAndFlightRequestId(42L, existingRequest.getId())
                .orElseThrow();
        assertEquals(RefreshMode.T72_REFRESH, updatedFollow.getRefreshMode());
        assertNotNull(updatedFollow.getBaselineSnapshotId());
    }

    @Test
    @DisplayName("Model port is called once for identical requests within the same bucket")
    void givenSameBucketRequests_whenPredict_thenUsesCache() throws Exception {
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestPayload()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestPayload()))
                .andExpect(status().isOk());

        verify(modelPredictionPort, times(1)).requestPrediction(any());
        assertEquals(1, flightPredictionJpaRepository.count());
    }

    @Test
    @WithMockUser(username = "42", roles = "USER")
    @DisplayName("Latest prediction creates bucket prediction without new user snapshot")
    void givenLatestPrediction_whenMissingBucket_thenCreatesPredictionWithoutNewSnapshot() throws Exception {
        FlightRequestEntity request = createExistingFlightRequest();
        FlightPredictionEntity existingPrediction = flightPredictionJpaRepository
                .save(createPrediction(request.getId(), BUCKET_START.minusHours(3)));
        userPredictionSnapshotJpaRepository.save(createUserSnapshot(42L, request.getId(), existingPrediction.getId()));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/predict/{requestId}/latest", request.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, flightPredictionJpaRepository.count());
        assertEquals(1, userPredictionSnapshotJpaRepository.count());
        assertEquals(true, flightPredictionJpaRepository
                .findByFlightRequestIdAndForecastBucketUtc(request.getId(), BUCKET_START)
                .isPresent());
        verify(modelPredictionPort, times(1)).requestPrediction(any());
    }

    @Test
    @WithMockUser(username = "42", roles = "USER")
    @DisplayName("Latest prediction uses cached bucket and does not call model")
    void givenLatestPrediction_whenBucketExists_thenUsesCache() throws Exception {
        FlightRequestEntity request = createExistingFlightRequest();
        FlightPredictionEntity cachedPrediction = flightPredictionJpaRepository
                .save(createPrediction(request.getId(), BUCKET_START));
        userPredictionSnapshotJpaRepository.save(createUserSnapshot(42L, request.getId(), cachedPrediction.getId()));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/predict/{requestId}/latest", request.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, flightPredictionJpaRepository.count());
        assertEquals(1, userPredictionSnapshotJpaRepository.count());
        verify(modelPredictionPort, times(0)).requestPrediction(any());
    }

    @Test
    @WithMockUser(username = "42", roles = "USER")
    @DisplayName("CSV import is forbidden for non-admin users")
    void givenNonAdmin_whenCsvImport_thenForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "predict.csv",
                "text/csv",
                buildCsvPayload().getBytes()
        );

        mockMvc.perform(multipart("/predict/bulk-import").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "42", roles = "ADMIN")
    @DisplayName("CSV import creates predictions, snapshots and subscriptions with cache")
    void givenAdmin_whenCsvImport_thenCreatesSnapshotsAndSubscriptions() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "predict.csv",
                "text/csv",
                buildCsvPayload().getBytes()
        );

        mockMvc.perform(multipart("/predict/bulk-import").file(file))
                .andExpect(status().isOk());

        assertEquals(1, flightRequestJpaRepository.count());
        assertEquals(1, flightPredictionJpaRepository.count());
        assertEquals(1, userPredictionSnapshotJpaRepository.count());
        assertEquals(1, flightSubscriptionJpaRepository.count());

        UserPredictionSnapshotEntity snapshot = userPredictionSnapshotJpaRepository.findAll().getFirst();
        assertEquals(com.flightontime.app_predictor.domain.model.UserPredictionSource.CSV_IMPORT, snapshot.getSource());

        FlightSubscriptionEntity follow = flightSubscriptionJpaRepository.findAll().getFirst();
        assertEquals(RefreshMode.T12_ONLY, follow.getRefreshMode());
        assertNotNull(follow.getBaselineSnapshotId());

        verify(modelPredictionPort, times(1)).requestPrediction(any());
    }

    private void seedAirports() {
        AirportEntity origin = new AirportEntity();
        origin.setAirportIata("EZE");
        origin.setAirportName("Ministro Pistarini");
        origin.setCityName("Buenos Aires");
        origin.setCountry("AR");
        origin.setLatitude(-34.8222);
        origin.setLongitude(-58.5358);

        AirportEntity dest = new AirportEntity();
        dest.setAirportIata("JFK");
        dest.setAirportName("John F Kennedy");
        dest.setCityName("New York");
        dest.setCountry("US");
        dest.setLatitude(40.6413);
        dest.setLongitude(-73.7781);

        airportJpaRepository.save(origin);
        airportJpaRepository.save(dest);
    }

    private void seedUser() {
        com.flightontime.app_predictor.infrastructure.out.entities.UserEntity user =
                new com.flightontime.app_predictor.infrastructure.out.entities.UserEntity();
        user.setId(42L);
        user.setEmail("user42@example.com");
        user.setPasswordHash("hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles("ROLE_USER");
        user.setCreatedAt(FIXED_NOW);
        userJpaRepository.save(user);
    }

    private FlightRequestEntity createExistingFlightRequest() {
        FlightRequestEntity request = new FlightRequestEntity();
        request.setFlightDateUtc(FLIGHT_DATE);
        request.setAirlineCode("AA");
        request.setOriginIata("EZE");
        request.setDestIata("JFK");
        request.setDistance(8500.0);
        request.setFlightNumber("102");
        request.setActive(true);
        return flightRequestJpaRepository.save(request);
    }

    private UserPredictionSnapshotEntity createUserSnapshot(Long userId, Long requestId, Long predictionId) {
        UserPredictionSnapshotEntity snapshot = new UserPredictionSnapshotEntity();
        snapshot.setUserId(userId);
        snapshot.setFlightRequestId(requestId);
        snapshot.setFlightPredictionId(predictionId);
        snapshot.setSource(com.flightontime.app_predictor.domain.model.UserPredictionSource.USER_QUERY);
        snapshot.setCreatedAt(FIXED_NOW);
        return snapshot;
    }

    private FlightPredictionEntity createPrediction(Long requestId, OffsetDateTime bucketStart) {
        FlightPredictionEntity prediction = new FlightPredictionEntity();
        prediction.setFlightRequestId(requestId);
        prediction.setForecastBucketUtc(bucketStart);
        prediction.setPredictedStatus("ON_TIME");
        prediction.setPredictedProbability(0.9);
        prediction.setConfidence("HIGH");
        prediction.setThresholdUsed(0.7);
        prediction.setModelVersion("v1");
        prediction.setSource(PredictionSource.SYSTEM);
        prediction.setPredictedAt(FIXED_NOW);
        prediction.setCreatedAt(FIXED_NOW);
        return prediction;
    }

    private String buildRequestPayload() {
        return """
                {
                  "flDate": "%s",
                  "carrier": "AA",
                  "origin": "EZE",
                  "dest": "JFK",
                  "flightNumber": "102"
                }
                """.formatted(FLIGHT_DATE);
    }

    private String buildCsvPayload() {
        List<String> lines = List.of(
                "fl_date_utc,carrier,origin,dest,flight_number",
                "2030-05-01T14:30:00Z,AA,EZE,JFK,102",
                "2030-05-01T14:30:00Z,AA,EZE,JFK,102"
        );
        return String.join("\n", lines);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2025-01-01T01:30:00Z"), ZoneOffset.UTC);
        }
    }
}
