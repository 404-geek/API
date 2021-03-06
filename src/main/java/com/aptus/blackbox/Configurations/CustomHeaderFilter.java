package com.aptus.blackbox.Configurations;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;

@Configuration
	public class CustomHeaderFilter  implements Filter{

	
	/*@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                .allowedOrigins("*");
            }
        };
    }*/



	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 * Filter chaining for passing headers to all HttpResponse and HttpRequests
	 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
    	
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        httpServletResponse.setHeader("access-control-allow-credentials", "true");
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        chain.doFilter(request, response);
    }
 
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // ...
    }
 
    @Override
    public void destroy() {
        // ...
    }


}
