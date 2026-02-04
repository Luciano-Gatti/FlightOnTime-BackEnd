package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.ports.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Clase PasswordEncoderAdapter.
 */
@Component
public class PasswordEncoderAdapter implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;

    /**
     * Ejecuta la operación password encoder adapter.
     * @param passwordEncoder variable de entrada passwordEncoder.
     */
    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Ejecuta la operación hash.
     * @param rawPassword variable de entrada rawPassword.
     * @return resultado de la operación hash.
     */
    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Ejecuta la operación matches.
     * @param rawPassword variable de entrada rawPassword.
     * @param hash variable de entrada hash.
     * @return resultado de la operación matches.
     */
    @Override
    public boolean matches(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }
}
