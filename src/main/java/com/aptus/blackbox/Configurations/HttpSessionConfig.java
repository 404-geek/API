package com.aptus.blackbox.Configurations;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

/*
 *   The @EnableMongoHttpSession annotation creates a Spring Bean
 *   with the name of springSessionRepositoryFilter that implements Filter. 
 *   The filter is what is in charge of replacing the HttpSession 
 *   implementation to be backed by Spring Session.
 *   In this instance Spring Session is backed by Mongo.
 */
@EnableMongoHttpSession(maxInactiveIntervalInSeconds=600)
public class HttpSessionConfig {

	public HttpSessionConfig() {
		// TODO Auto-generated constructor stub
		
	}

	/*
	 * We explicitly configure JdkMongoSessionConverter since Spring Security’s
	 * objects cannot be automatically persisted using Jackson
	 */
	@Bean
	public JdkMongoSessionConverter jdkMongoSessionConverter() {
		
		return new JdkMongoSessionConverter();
	}

}
