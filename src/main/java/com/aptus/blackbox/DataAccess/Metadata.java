package com.aptus.blackbox.DataAccess;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;

abstract class Metadata {
	
	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	protected  ResponseEntity<String> getUserDetails(String url,String userId) 
	{
		ResponseEntity<String> out = null;
        HttpHeaders headers = new HttpHeaders();
		try {
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
			JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Sucessfully displaying user details");
            respBody.addProperty("status", "200");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
	}
	catch (Exception e) {
        e.printStackTrace();
    }
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	

	protected ResponseEntity<String> getDetails(String url,String userId) 
	{ 
		ResponseEntity<String> out = null;
        HttpHeaders headers = new HttpHeaders();
		try {
	        headers.add("Cache-Control", "no-cache");
	        headers.add("access-control-allow-origin", rootUrl);
	        headers.add("access-control-allow-credentials", "true");
	        url = mongoUrl + "/credentials/userCredentials";
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            System.out.println(uri);
            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			RestTemplate restTemplate = new RestTemplate();
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
			JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Sucessfully displaying all details");
            respBody.addProperty("status", "200");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
		
		}
	catch (Exception e) {
        e.printStackTrace();
    }   
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	protected ResponseEntity<String> postUserDetails(String url,String userId) 
	{ 
		ResponseEntity<String> out = null;
        HttpHeaders headers = new HttpHeaders();
		try {
	        headers.add("Cache-Control", "no-cache");
	        headers.add("access-control-allow-origin", rootUrl);
	        headers.add("access-control-allow-credentials", "true");
	        url = mongoUrl + "/credentials/userCredentials";
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            System.out.println(uri);
            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			RestTemplate restTemplate = new RestTemplate();
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
			JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Sucessfully displaying all details");
            respBody.addProperty("status", "200");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
		
		}
	catch (Exception e) {
        e.printStackTrace();
    }   
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	
	
}
