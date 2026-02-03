package com.flightontime.app_predictor.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase ApiMarketProperties.
 */
@ConfigurationProperties(prefix = "api.market")
public class ApiMarketProperties {
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
