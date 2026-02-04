package com.flightontime.app_predictor.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase AeroDataBoxProperties.
 */
@ConfigurationProperties(prefix = "aerodatabox.service")
public class AeroDataBoxProperties {
    private String url;

    /**
     * Ejecuta la operación get url.
     * @return resultado de la operación get url.
     */

    public String getUrl() {
        return url;
    }

    /**
     * Ejecuta la operación set url.
     * @param url variable de entrada url.
     */

    public void setUrl(String url) {
        this.url = url;
    }
}
