package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.model.UserAuthData;
import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Clase AuthService.
 */
@Service
public class AuthService {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construye el servicio de autenticación.
     *
     * @param userRepositoryPort repositorio de usuarios.
     * @param passwordEncoder encoder de contraseñas.
     */
    public AuthService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
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
        if (userRepositoryPort.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        User user = new User(
                null,
                email,
                firstName,
                lastName,
                "ROLE_USER",
                createdAt
        );
        return userRepositoryPort.save(user, passwordEncoder.encode(rawPassword));
    }

    /**
     * Autentica un usuario por email y contraseña.
     *
     * @param email email del usuario.
     * @param rawPassword contraseña en texto plano.
     * @return usuario autenticado.
     */
    public User login(String email, String rawPassword) {
        UserAuthData authData = userRepositoryPort.findAuthDataByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, authData.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
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
}
