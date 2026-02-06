package com.flightspredictor.flights.infra.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspredictor.flights.domain.dto.auth.LoginResponse;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.domain.service.prediction.PredictionService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User(
                null,
                "user@example.com",
                passwordEncoder.encode("password"),
                "Test",
                "User",
                "ROLE_USER",
                null,
                null,
                null,
                null
        );
        userRepository.save(user);
    }

    @Test
    void predictWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildPredictionRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void predictWithValidTokenReturnsOk() throws Exception {
        when(predictionService.predict(any())).thenReturn(
                new ModelPredictionResponse(PredictedStatus.ON_TIME, 0.85, "0.85")
        );

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse response = objectMapper.readValue(loginResponse, LoginResponse.class);

        mockMvc.perform(post("/predict")
                        .header("Authorization", response.tokenType() + " " + response.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildPredictionRequest()))
                .andExpect(status().isOk());
    }

    private String buildPredictionRequest() {
        String futureDate = OffsetDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return "{"
                + "\"fl_date\":\"" + futureDate + "\"," 
                + "\"op_unique_carrier\":\"AA\"," 
                + "\"origin\":\"JFK\"," 
                + "\"dest\":\"LAX\""
                + "}";
    }
}
