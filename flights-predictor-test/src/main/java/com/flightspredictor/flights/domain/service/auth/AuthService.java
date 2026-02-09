package com.flightspredictor.flights.domain.service.auth;

import com.flightspredictor.flights.domain.dto.auth.LoginRequest;
import com.flightspredictor.flights.domain.dto.auth.LoginResponse;
<<<<<<< codex/modify-jwt-auth-for-optional-authentication-2c5df2
import com.flightspredictor.flights.domain.dto.auth.RegisterRequest;
import com.flightspredictor.flights.domain.dto.auth.RegisterResponse;
import com.flightspredictor.flights.domain.dto.auth.RegisterUserResponse;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.infra.security.InvalidCredentialsException;
import com.flightspredictor.flights.infra.security.InvalidPasswordException;
import com.flightspredictor.flights.infra.security.JwtProperties;
import com.flightspredictor.flights.infra.security.JwtService;
import com.flightspredictor.flights.infra.security.UserEmailAlreadyExistsException;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
=======
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.infra.security.InvalidCredentialsException;
import com.flightspredictor.flights.infra.security.JwtProperties;
import com.flightspredictor.flights.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
>>>>>>> main
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
<<<<<<< codex/modify-jwt-auth-for-optional-authentication-2c5df2
    private final int passwordMinLength;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProperties jwtProperties,
            @Value("${app.security.password.min-length:8}") int passwordMinLength
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
        this.passwordMinLength = passwordMinLength;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
=======

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
>>>>>>> main
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user);
        long expiresInMinutes = (long) Math.ceil(jwtProperties.expirationSeconds() / 60.0);
        return new LoginResponse(token, "Bearer", expiresInMinutes);
<<<<<<< codex/modify-jwt-auth-for-optional-authentication-2c5df2
    }

    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String correlationId = MDC.get("correlationId");
        log.info("AUTH_REGISTER_START correlationId={} email={}", correlationId, normalizedEmail);

        try {
            validatePassword(request.password());
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new UserEmailAlreadyExistsException(normalizedEmail);
            }
            User user = new User(
                    null,
                    normalizedEmail,
                    passwordEncoder.encode(request.password()),
                    request.firstName().trim(),
                    request.lastName().trim(),
                    "ROLE_USER",
                    null,
                    null,
                    null,
                    null
            );
            User savedUser = userRepository.save(user);
            String token = jwtService.generateToken(savedUser);
            RegisterUserResponse userResponse = new RegisterUserResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    List.of("ROLE_USER")
            );
            log.info("AUTH_REGISTER_OK correlationId={} userId={}", correlationId, savedUser.getId());
            return new RegisterResponse(userResponse, token, "Bearer");
        } catch (RuntimeException ex) {
            String errorCode = "INTERNAL_ERROR";
            if (ex instanceof UserEmailAlreadyExistsException) {
                errorCode = "USER_EMAIL_ALREADY_EXISTS";
            } else if (ex instanceof InvalidPasswordException) {
                errorCode = "USER_PASSWORD_INVALID";
            }
            log.warn("AUTH_REGISTER_FAIL correlationId={} errorCode={}", correlationId, errorCode);
            throw ex;
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException("Password is required");
        }
        if (password.length() < passwordMinLength) {
            throw new InvalidPasswordException("Password must be at least " + passwordMinLength + " characters");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordException("Password must contain letters and numbers");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
=======
>>>>>>> main
    }
}
