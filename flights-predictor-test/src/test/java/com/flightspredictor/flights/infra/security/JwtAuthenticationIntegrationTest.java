package com.flightspredictor.flights.infra.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.domain.service.prediction.PredictionService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthenticationIntegrationTest.TestHistoryController.class)
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PredictionService predictionService;

    private String validToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User(
                null,
                "user@example.com",
                "hashed",
                "Test",
                "User",
                "ROLE_USER",
                null,
                null,
                null,
                null
        );
        User savedUser = userRepository.save(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        validToken = jwtService.generateToken(authentication);

        when(predictionService.predict(any())).thenReturn(
                new ModelPredictionResponse(PredictedStatus.ON_TIME, 0.8, "HIGH")
        );
    }

    @Test
    void predictWithoutTokenReturnsOk() throws Exception {
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void predictWithValidTokenReturnsOk() throws Exception {
        mockMvc.perform(post("/predict")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void predictWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/predict")
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historyWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historyWithRoleUserReturnsOk() throws Exception {
        mockMvc.perform(get("/history")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    private PredictionRequest buildRequest() {
        return new PredictionRequest(
                OffsetDateTime.now().plusDays(1),
                "AA",
                "JFK",
                "LAX"
        );
    }

    @TestConfiguration
    @RestController
    static class TestHistoryController {

        @GetMapping("/history")
        public String history() {
            return "ok";
        }
    }
}
