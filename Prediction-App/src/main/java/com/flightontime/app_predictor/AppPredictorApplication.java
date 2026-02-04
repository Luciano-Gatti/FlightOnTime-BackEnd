package com.flightontime.app_predictor;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

/**
 * Clase AppPredictorApplication.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AppPredictorApplication {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	public static void main(String[] args) {
		SpringApplication.run(AppPredictorApplication.class, args);
	}

}
