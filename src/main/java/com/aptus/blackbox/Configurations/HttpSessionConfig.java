package com.aptus.blackbox.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;


/*
 * 	The @EnableMongoHttpSession annotation 
 *  creates a Spring Bean with the name of springSessionRepositoryFilter
 */
@EnableMongoHttpSession 
	public class HttpSessionConfig {
		
		
		
		public HttpSessionConfig() {
			// TODO Auto-generated constructor stub
		}
	    
		@Bean
	    public JdkMongoSessionConverter jdkMongoSessionConverter() {
	                return new JdkMongoSessionConverter(); 
	        }


}
