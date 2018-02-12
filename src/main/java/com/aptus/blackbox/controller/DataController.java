package com.aptus.blackbox.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.Service.Credentials;

@RestController
public class DataController {
	
	private String mongoUrl;
	
	public DataController(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	@Autowired
	private Credentials credentials;
	
	@RequestMapping(method = RequestMethod.GET, value = "/authdesination")
	private void destination(@RequestParam("data") String data) {
		try {
			ResponseEntity<String> out = null;
			RestTemplate restTemplate = new RestTemplate();
			String url ="http://"+mongoUrl+"/credentials/userCredentials";
			HttpHeaders headers =new HttpHeaders();
	        headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
	        headers.add("Cache-Control", "no-cache");
	        HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);	        
	        out = restTemplate.exchange(URI.create(url), HttpMethod.GET, httpEntity, String.class);
	        if(out.getStatusCode().is2xxSuccessful()) {
	        	
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
}
