package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.AuthResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.LoginRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.RegisterRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.UserResponseDTO;
import com.flightontime.app_predictor.infrastructure.out.entities.UserEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.UserJpaRepository;
import com.flightontime.app_predictor.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Clase AuthController.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Operaciones de autenticación")
public class AuthController {
    private final UserRepositoryPort userRepositoryPort;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long expirationMinutes;

    public AuthController(
            UserRepositoryPort userRepositoryPort,
            UserJpaRepository userJpaRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.expirationMinutes = expirationMinutes;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = "Crea una cuenta de usuario y devuelve el token JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepositoryPort.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        User user = new User(
                null,
                request.email(),
                request.firstName(),
                request.lastName(),
                "ROLE_USER",
                createdAt
        );
        User savedUser = userRepositoryPort.save(user, passwordEncoder.encode(request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(savedUser));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida credenciales y devuelve un token JWT válido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        UserEntity entity = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), entity.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        User user = new User(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRoles(),
                entity.getCreatedAt()
        );
        return ResponseEntity.ok(buildAuthResponse(user));
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.id(), user.roles(), user.email());
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(expirationMinutes);
        UserResponseDTO userResponse = new UserResponseDTO(
                user.id(),
                user.email(),
                user.firstName(),
                user.lastName(),
                user.roles()
        );
        return new AuthResponseDTO(token, expiresAt, userResponse);
    }
}
