package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.model.UserAuthData;
import com.flightontime.app_predictor.domain.ports.out.PasswordHasherPort;
import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

/**
 * Clase AuthService.
 */
@Service
public class AuthService {
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
        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email already registered");
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
        return userRepositoryPort.save(user, passwordHasherPort.hash(rawPassword));
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
}
