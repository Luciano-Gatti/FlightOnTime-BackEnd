package com.flightontime.app_predictor.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {
    private String secret;
    private long expirationMinutes;
    private boolean secretBase64;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public boolean isSecretBase64() {
        return secretBase64;
    }

    public void setSecretBase64(boolean secretBase64) {
        this.secretBase64 = secretBase64;
    }
}
