package com.flightontime.app_predictor.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase SecurityJwtProperties.
 */
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {
    private String secret;
    private long expirationMinutes;
    private boolean secretBase64;

    /**
     * Ejecuta la operación get secret.
     * @return resultado de la operación get secret.
     */

    public String getSecret() {
        return secret;
    }

    /**
     * Ejecuta la operación set secret.
     * @param secret variable de entrada secret.
     */

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Ejecuta la operación get expiration minutes.
     * @return resultado de la operación get expiration minutes.
     */

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    /**
     * Ejecuta la operación set expiration minutes.
     * @param expirationMinutes variable de entrada expirationMinutes.
     */

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Ejecuta la operación is secret base64.
     * @return resultado de la operación is secret base64.
     */

    public boolean isSecretBase64() {
        return secretBase64;
    }

    /**
     * Ejecuta la operación set secret base64.
     * @param secretBase64 variable de entrada secretBase64.
     */

    public void setSecretBase64(boolean secretBase64) {
        this.secretBase64 = secretBase64;
    }
}
