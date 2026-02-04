package com.flightontime.app_predictor.domain.ports.out;

/**
 * Interfaz PasswordHasherPort.
 */
public interface PasswordHasherPort {
    /**
     * Ejecuta la operación hash.
     * @param rawPassword variable de entrada rawPassword.
     * @return resultado de la operación hash.
     */
    String hash(String rawPassword);

    /**
     * Ejecuta la operación matches.
     * @param rawPassword variable de entrada rawPassword.
     * @param hash variable de entrada hash.
     * @return resultado de la operación matches.
     */
    boolean matches(String rawPassword, String hash);
}
