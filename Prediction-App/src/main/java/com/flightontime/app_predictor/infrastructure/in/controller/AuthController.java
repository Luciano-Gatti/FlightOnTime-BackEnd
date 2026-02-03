package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.application.services.AuthService;
import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.infrastructure.in.dto.AuthResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.LoginRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.RegisterRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.UserResponseDTO;
import com.flightontime.app_predictor.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase AuthController.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Operaciones de autenticación")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final long expirationMinutes;

    public AuthController(
            AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.authService = authService;
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
        User savedUser = authService.register(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password()
        );
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
        User user = authService.login(request.email(), request.password());
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
