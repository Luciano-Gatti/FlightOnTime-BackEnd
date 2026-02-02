package com.flightontime.app_predictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AppPredictorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppPredictorApplication.class, args);
	}

}
