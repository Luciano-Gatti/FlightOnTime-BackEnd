package com.flightspredictor.flights.infra.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.entities.FlightRequest;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.entities.UserPredictionSnapshot;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.enums.SnapshotSource;
import com.flightspredictor.flights.domain.repository.FlightPredictionRepository;
import com.flightspredictor.flights.domain.repository.FlightRequestRepository;
import com.flightspredictor.flights.domain.repository.UserPredictionSnapshotRepository;
import com.flightspredictor.flights.domain.repository.UserRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRequestRepository requestRepository;

    @Autowired
    private FlightPredictionRepository predictionRepository;

    @Autowired
    private UserPredictionSnapshotRepository snapshotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        snapshotRepository.deleteAll();
        predictionRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User(
                null,
                "history@example.com",
                passwordEncoder.encode("secret"),
                "Test",
                "User",
                "ROLE_USER",
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void historyWithTokenReturnsPaginatedResults() throws Exception {
        FlightRequest request = requestRepository.save(new FlightRequest(
                new PredictionRequest(
                        OffsetDateTime.now().plusDays(1),
                        "AA",
                        "JFK",
                        "LAX"
                ),
                100.0
        ));
        FlightPrediction prediction = predictionRepository.save(new FlightPrediction(
                new ModelPredictionResponse(PredictedStatus.ON_TIME, 0.9, "HIGH"),
                request
        ));
        snapshotRepository.save(new UserPredictionSnapshot(
                null,
                user,
                request,
                prediction,
                null,
                SnapshotSource.USER_QUERY
        ));

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].predictionId").value(prediction.getId()))
                .andExpect(jsonPath("$.size").value(5));
    }
}
