package com.aptus.blackbox;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties.Credential;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class BlackBoxReloadedApp extends SpringBootServletInitializer{
	
	
	public static void main(String[] args)throws IOException {
		final Logger logger = LogManager.getLogger(BlackBoxReloadedApp.class.getPackage());
		 
		
	     ThreadContext.put("id", "192.168.21.9");
		 logger.info("INFO MSG");
		 logger.debug("debug MSG");
		 logger.error("error MSG");
		 ThreadContext.clearAll();
		 logger.warn("warn MSG");
		 logger.trace("trac  msg");
		
		SpringApplication.run(BlackBoxReloadedApp.class, args);
		 logger.info("Spring started");
		
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BlackBoxReloadedApp.class);
	}
}
