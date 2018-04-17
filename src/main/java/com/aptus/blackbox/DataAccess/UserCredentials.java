package com.aptus.blackbox.DataAccess;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

abstract class Metadata {
	
	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	protected void getDetails(String url,String userId) 
	{try 
	{
		 ResponseEntity<String> out = null;
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Cache-Control", "no-cache");
	        headers.add("access-control-allow-origin", rootUrl);
	        headers.add("access-control-allow-credentials", "true");
	        String filter = "{\"_id\":\"" + userId.toLowerCase() + "\"}";
	        url = mongoUrl + "/credentials/userCredentials?filter=" + filter;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            System.out.println(uri);
            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			RestTemplate restTemplate = new RestTemplate();
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
	}
	catch (Exception e) {
        e.printStackTrace();
    }   
	}
	
}
