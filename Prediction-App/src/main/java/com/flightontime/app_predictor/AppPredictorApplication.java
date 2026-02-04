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

	/**
	 * Ejecuta la operación clock.
	 * @return resultado de la operación clock.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	/**
	 * Ejecuta la operación main.
	 * @param args variable de entrada args.
	 */

	public static void main(String[] args) {
		SpringApplication.run(AppPredictorApplication.class, args);
	}

}
