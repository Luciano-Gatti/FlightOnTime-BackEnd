package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.application.services.UserLookupService;
import com.flightontime.app_predictor.domain.ports.in.BulkPredictUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import com.flightontime.app_predictor.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictFlightUseCase predictFlightUseCase;

    @MockBean
    private PredictHistoryUseCase predictHistoryUseCase;

    @MockBean
    private BulkPredictUseCase bulkPredictUseCase;

    @MockBean
    private UserLookupService userLookupService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ModelPredictionPort modelPredictionPort;

    @MockBean
    private WeatherPort weatherPort;

    @MockBean
    private AirportInfoPort airportInfoPort;

    @MockBean
    private NotificationPort notificationPort;

    @Test
    @DisplayName("Returns 400 with field details when origin IATA is invalid")
    void givenInvalidOriginIata_whenPredict_thenReturnsBadRequest() throws Exception {
        String payload = buildRequestPayload("EZ", "AA", "JFK", "2030-05-01T14:30:00Z");

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.originIata").exists());
    }

    @Test
    @DisplayName("Returns 400 when airline code is invalid")
    void givenInvalidAirlineCode_whenPredict_thenReturnsBadRequest() throws Exception {
        String payload = buildRequestPayload("EZE", "AAA", "JFK", "2030-05-01T14:30:00Z");

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.airlineCode").exists());
    }

    @Test
    @DisplayName("Returns 400 when flight date is in the past")
    void givenPastFlightDate_whenPredict_thenReturnsBadRequest() throws Exception {
        String payload = buildRequestPayload("EZE", "AA", "JFK", "2000-01-01T00:00:00Z");

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.flightDateUtc").exists());
    }

    private String buildRequestPayload(
            String origin,
            String carrier,
            String dest,
            String flightDate
    ) {
        return """
                {
                  "flDate": "%s",
                  "carrier": "%s",
                  "origin": "%s",
                  "dest": "%s",
                  "flightNumber": "102"
                }
                """.formatted(flightDate, carrier, origin, dest);
    }
}
