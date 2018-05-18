package com.aptus.blackbox;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import com.aptus.blackbox.dataService.Config;

@SpringBootApplication
public class BlackBoxReloadedApp extends SpringBootServletInitializer{
	
	
	public static void main(String[] args)throws IOException {
		SpringApplication.run(BlackBoxReloadedApp.class, args);
		
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BlackBoxReloadedApp.class);
	}
}
