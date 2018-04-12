package com.aptus.blackbox.controller;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class adminController {

	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	@RequestMapping(method = RequestMethod.POST, value = "/addsrcdest")
	private ResponseEntity<String> addSource(@RequestBody String body,@RequestParam("type") String type, @RequestParam("choice") String choice, @RequestParam("sourcename") String srcdestname)
	{
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		headers.add("Content-Type", "application/json");
		String url;
		HttpEntity<?> httpEntity ;
		
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        if(choice.equals("add"))
        	{url=mongoUrl+"/credentials/"+type;
			httpEntity = new HttpEntity<String>(body,headers);
       
        	out = restTemplate.exchange(URI.create(url), HttpMethod.POST, httpEntity, String.class);
        	 JsonObject respBody = new JsonObject();

     		respBody.addProperty("status", "200");
     		respBody.addProperty("message", type +" successfully added");
     		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        	}
        else if(choice.equals("update"))
        {	url=mongoUrl+"/credentials/"+type+srcdestname;
        httpEntity = new HttpEntity<String>(body,headers);
        	out=restTemplate.exchange(URI.create(url), HttpMethod.PATCH, httpEntity, String.class);
        	 JsonObject respBody = new JsonObject();

     		respBody.addProperty("status", "200");
     		respBody.addProperty("message", type +" successfully updated");
     		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
        }
	
	@RequestMapping(method =RequestMethod.GET,value="/manageclient" )
	private ResponseEntity<String> manageClient()
	{
		
	}

}
