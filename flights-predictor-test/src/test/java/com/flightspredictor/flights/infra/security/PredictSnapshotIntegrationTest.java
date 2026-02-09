package com.flightspredictor.flights.infra.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.dto.prediction.PredictionResponse;
import com.flightspredictor.flights.domain.entities.Airport;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.repository.FlightPredictionRepository;
import com.flightspredictor.flights.domain.repository.UserPredictionSnapshotRepository;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.domain.service.prediction.AirportLookupService;
import com.flightspredictor.flights.infra.external.prediction.client.PredictionApiClient;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictSnapshotIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPredictionSnapshotRepository snapshotRepository;

    @Autowired
    private FlightPredictionRepository predictionRepository;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AirportLookupService airportLookupService;

    @MockBean
    private PredictionApiClient predictionApiClient;

    @BeforeEach
    void setUp() {
        snapshotRepository.deleteAll();
        predictionRepository.deleteAll();
        userRepository.deleteAll();
        when(airportLookupService.getAirport("JFK")).thenReturn(buildAirport("JFK"));
        when(airportLookupService.getAirport("LAX")).thenReturn(buildAirport("LAX"));
        when(predictionApiClient.predict(any())).thenReturn(new PredictionResponse("On Time", 0.8, "HIGH"));
    }

    @Test
    void predictWithoutTokenDoesNotCreateSnapshot() throws Exception {
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(snapshotRepository.count()).isZero();
    }

    @Test
    void predictWithTokenCreatesSnapshot() throws Exception {
        User user = userRepository.save(new User(
                null,
                "snapshot@example.com",
                "hashed",
                "Test",
                "User",
                "ROLE_USER",
                null,
                null,
                null,
                null
        ));
        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/predict")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(snapshotRepository.count()).isEqualTo(1);
    }

    private PredictionRequest buildRequest() {
        return new PredictionRequest(
                OffsetDateTime.now().plusDays(1),
                "AA",
                "JFK",
                "LAX"
        );
    }

    private Airport buildAirport(String iata) {
        return new Airport(
                null,
                iata,
                "Airport",
                "US",
                "City",
                -73.0,
                40.0,
                10.0,
                "America/New_York",
                "https://maps.example.com"
        );
    }
}
