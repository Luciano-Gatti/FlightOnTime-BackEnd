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

    /**
     * Ejecuta la operación user lookup service.
     * @param userRepositoryPort variable de entrada userRepositoryPort.
     */

    /**
     * Ejecuta la operación user lookup service.
     * @param userRepositoryPort variable de entrada userRepositoryPort.
     * @return resultado de la operación user lookup service.
     */

    public UserLookupService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    /**
     * Ejecuta la operación find user id by email.
     * @param email variable de entrada email.
     * @return resultado de la operación find user id by email.
     */

    public Optional<Long> findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepositoryPort.findByEmail(email)
                .map(user -> user.id());
    }
}
