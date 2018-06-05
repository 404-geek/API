package com.aptus.blackbox;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class BlackBoxReloadedApp extends SpringBootServletInitializer{
	
	
	public static void main(String[] args)throws IOException {
		final Logger logger = LogManager.getLogger(BlackBoxReloadedApp.class.getPackage());
		 logger.info("INFO MSG");
		 logger.debug("debug MSG");
		 logger.error("error MSG");
		SpringApplication.run(BlackBoxReloadedApp.class, args);
		
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BlackBoxReloadedApp.class);
	}
}
