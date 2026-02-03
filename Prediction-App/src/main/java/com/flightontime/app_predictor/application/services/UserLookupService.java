package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Clase UserLookupService.
 */
@Service
public class UserLookupService {
    private final UserRepositoryPort userRepositoryPort;

    public UserLookupService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public Optional<Long> findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepositoryPort.findByEmail(email)
                .map(user -> user.id());
    }
}
