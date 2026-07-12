package com.stdace.neuroforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NeuroForgeEnterpriseSdlcPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeuroForgeEnterpriseSdlcPlatformApplication.class, args);
	}

}
