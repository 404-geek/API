	package com.aptus.blackbox.security;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.aptus.blackbox")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

 
    @Override
    protected void configure(
      AuthenticationManagerBuilder auth) throws Exception {
  
    }
 
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.csrf().disable();
    	//http.cors().disable();
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        http.sessionManagement().invalidSessionUrl("/index.html").maximumSessions(1).expiredUrl("/close.html");
       http.sessionManagement().sessionFixation().migrateSession();	
        http.sessionManagement().enableSessionUrlRewriting(false);
       
    }


}
