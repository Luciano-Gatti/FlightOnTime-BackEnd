package com.flightspredictor.flights.domain.service.auth;

import com.flightspredictor.flights.domain.dto.auth.LoginRequest;
import com.flightspredictor.flights.domain.dto.auth.LoginResponse;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.infra.security.InvalidCredentialsException;
import com.flightspredictor.flights.infra.security.JwtProperties;
import com.flightspredictor.flights.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user);
        long expiresInMinutes = (long) Math.ceil(jwtProperties.expirationSeconds() / 60.0);
        return new LoginResponse(token, "Bearer", expiresInMinutes);
    }
}
