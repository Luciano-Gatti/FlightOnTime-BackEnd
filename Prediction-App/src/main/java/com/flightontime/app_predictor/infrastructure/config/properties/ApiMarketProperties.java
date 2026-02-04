package com.flightontime.app_predictor.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase ApiMarketProperties.
 */
@ConfigurationProperties(prefix = "api.market")
public class ApiMarketProperties {
    private String key;

    /**
     * Ejecuta la operación get key.
     * @return resultado de la operación get key.
     */

    public String getKey() {
        return key;
    }

    /**
     * Ejecuta la operación set key.
     * @param key variable de entrada key.
     */

    public void setKey(String key) {
        this.key = key;
    }
}
