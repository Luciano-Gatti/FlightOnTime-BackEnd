package com.flightspredictor.flights.domain.service.auth;

import com.flightspredictor.flights.domain.dto.auth.LoginRequest;
import com.flightspredictor.flights.domain.dto.auth.LoginResponse;
import com.flightspredictor.flights.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtService.generateToken(authentication);
        return new LoginResponse(token, "Bearer");
    }
}
