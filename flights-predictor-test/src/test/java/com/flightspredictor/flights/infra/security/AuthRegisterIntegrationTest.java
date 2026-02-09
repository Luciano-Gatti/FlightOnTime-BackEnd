package com.flightspredictor.flights.infra.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspredictor.flights.domain.dto.auth.RegisterRequest;
import com.flightspredictor.flights.domain.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserWithRoleUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "user@mail.com",
                "Password123",
                "Juan",
                "Pérez"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.email").value("user@mail.com"))
                .andExpect(jsonPath("$.user.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        Assertions.assertThat(userRepository.findByEmailIgnoreCase("user@mail.com"))
                .isPresent()
                .get()
                .satisfies(user -> {
                    Assertions.assertThat(user.getPasswordHash()).isNotBlank();
                    Assertions.assertThat(user.getRoles()).isEqualTo("ROLE_USER");
                });
    }

    @Test
    void registerWithDuplicateEmailReturnsConflict() throws Exception {
        userRepository.save(new com.flightspredictor.flights.domain.entities.User(
                null,
                "user@mail.com",
                "hashed",
                "Juan",
                "Pérez",
                "ROLE_USER",
                null,
                null,
                null,
                null
        ));

        RegisterRequest request = new RegisterRequest(
                "user@mail.com",
                "Password123",
                "Juan",
                "Pérez"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void registerWithInvalidPasswordReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "user2@mail.com",
                "password",
                "Juan",
                "Pérez"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("USER_PASSWORD_INVALID"));
    }
}
