package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.model.UserAuthData;
import com.flightontime.app_predictor.domain.ports.out.PasswordHasherPort;
import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Clase AuthService.
 */
@Service
public class AuthService {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    /**
     * Construye el servicio de autenticación.
     *
     * @param userRepositoryPort repositorio de usuarios.
     * @param passwordHasherPort encoder de contraseñas.
     */
    public AuthService(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    /**
     * Registra un usuario nuevo con rol por defecto.
     *
     * @param email email del usuario.
     * @param firstName nombre del usuario.
     * @param lastName apellido del usuario.
     * @param rawPassword contraseña en texto plano.
     * @return usuario persistido.
     */
    public User register(
            String email,
            String firstName,
            String lastName,
            String rawPassword
    ) {
        String normalizedEmail = normalizeEmail(email);
        String fn = requireTrimmed("firstName", firstName);
        String ln = requireTrimmed("lastName", lastName);
        validateRequired("rawPassword", rawPassword);
        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        User user = new User(
                null,
                normalizedEmail,
                fn,
                ln,
                DEFAULT_ROLE,
                createdAt
        );
        return userRepositoryPort.save(user, passwordHasherPort.hash(rawPassword));
    }

    private String requireTrimmed(String field, String value) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        String v = value.trim();
        if (v.isEmpty()) throw new IllegalArgumentException(field + " is required");
        return v;
    }
    
    /**
     * Autentica un usuario por email y contraseña.
     *
     * @param email email del usuario.
     * @param rawPassword contraseña en texto plano.
     * @return usuario autenticado.
     */
    public User login(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        validateRequired("rawPassword", rawPassword);
        UserAuthData authData = userRepositoryPort.findAuthDataByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if (!passwordHasherPort.matches(rawPassword, authData.passwordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return new User(
                authData.id(),
                authData.email(),
                authData.firstName(),
                authData.lastName(),
                authData.roles(),
                authData.createdAt()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email is required");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        return normalized;
    }

    private void validateRequired(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
