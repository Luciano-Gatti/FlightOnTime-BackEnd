package com.flightontime.app_predictor.infrastructure.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Clase WebClientConfig.
 */
@Configuration
public class WebClientConfig {

    private HttpClient buildHttpClient(Duration connectTimeout, Duration readTimeout) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                .responseTimeout(readTimeout);
    }

    @Bean
    public WebClient modelWebClient(
            @Value("${model.service.url}") String modelServiceUrl,
            @Value("${webclient.timeout.connect}") Duration connectTimeout,
            @Value("${webclient.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .baseUrl(modelServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient airportWebClient(
            @Value("${airport.service.url}") String airportServiceUrl,
            @Value("${webclient.timeout.connect}") Duration connectTimeout,
            @Value("${webclient.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .baseUrl(airportServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient weatherWebClient(
            @Value("${weather.service.base-url}") String weatherServiceBaseUrl,
            @Value("${weather.service.timeout.connect}") Duration connectTimeout,
            @Value("${weather.service.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .baseUrl(weatherServiceBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient openMeteoWeatherWebClient(
            @Value("${weather.openmeteo.base-url}") String openMeteoBaseUrl,
            @Value("${weather.service.timeout.connect}") Duration connectTimeout,
            @Value("${weather.service.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .baseUrl(openMeteoBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient fallbackWeatherWebClient(
            @Value("${weather.fallback.timeout.connect}") Duration connectTimeout,
            @Value("${weather.fallback.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient aeroDataBoxWebClient(
            @Value("${aerodatabox.service.url}") String aeroDataBoxServiceUrl,
            @Value("${webclient.timeout.connect}") Duration connectTimeout,
            @Value("${webclient.timeout.read}") Duration readTimeout
    ) {
        HttpClient httpClient = buildHttpClient(connectTimeout, readTimeout);
        return WebClient.builder()
                .baseUrl(aeroDataBoxServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
