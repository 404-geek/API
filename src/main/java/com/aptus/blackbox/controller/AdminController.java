package com.aptus.blackbox.controller;

import java.net.URI;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.DestinationConfigService;
import com.aptus.blackbox.dataServices.SourceConfigService;
import com.aptus.blackbox.dataServices.SourceDestinationList;
import com.aptus.blackbox.datamodels.Categories;
import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.Destinations;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.datamodels.Sources;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class AdminController {

	@Autowired
	private Config config;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private Credentials credentials;
	
	@Autowired
	private SourceDestinationList sourcedestinationService;
	
	@Autowired
	private SourceConfigService sourceConfigService;
	
	@Autowired
	private DestinationConfigService destinationConfigService;
	
	
	public  AdminController() {
		System.out.println("Admin Controller Constructor");
	}
	
	@RequestMapping(method = RequestMethod.POST,value="/insert/{type}")
	private ResponseEntity<String> addData(@PathVariable String type, @RequestBody String data,HttpSession session){
		HttpHeaders headers = new HttpHeaders();
	
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
	
				
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message",Constants.SUCCESS_MSG);
				respBody.addProperty("status", Constants.SUCCESS_CODE);
				switch(type) {
				
				case "source":{
					Sources source = new Gson().fromJson(data, Sources.class);
					sourcedestinationService.insertSource(source);
					break;
					
				}
				case "destination":{
					Destinations destination = new Gson().fromJson(data, Destinations.class);
					sourcedestinationService.insertDestination(destination);
					break;
				}
				
				case "category":{
					Categories category = new Gson().fromJson(data,Categories.class);
					sourcedestinationService.insertCategory(category);
					break;
				}
				
				case "sourceConfig":{
					SourceConfig sourceConfig = new Gson().fromJson(data, SourceConfig.class);
					sourceConfigService.createSourceConfig(sourceConfig);
					break;
				}
				
				case "destinationConfig":{
					DestinationConfig destinationConfig = new Gson().fromJson(data, DestinationConfig.class);
					destinationConfigService.createDestinationConfig(destinationConfig);
					break;
				}
				default:{
					respBody.addProperty("message", Constants.FAILED_MSG);
					respBody.addProperty("status", Constants.FAILED_CODE);
				}
				
				}
				
				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
				
				
				
			}else {

				session.invalidate();
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			
			
			}
		}catch(Exception e){}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	

/*
	@RequestMapping(method =RequestMethod.GET,value="/viewdatasource" )
	private ResponseEntity<String> viewDataSource(@RequestParam("userId") String userId)
	{
		  ResponseEntity<String> out = null;
	        HttpHeaders headers = new HttpHeaders();
//	        headers.add("Cache-Control", "no-cache");
//	        headers.add("access-control-allow-origin", config.getRootUrl());
//	        headers.add("access-control-allow-credentials", "true");
	        try {         
	        	
	        	String filter = "{\"_id\":\"" + userId.toLowerCase() + "\"}";
				String url = config.getMongoUrl() + "/credentials/userCredentials?filter=" + filter;
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
	            System.out.println("viewdatasource fror admin");
	            System.out.println(uri);
	            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				RestTemplate restTemplate = new RestTemplate();
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				JsonObject respBody = new JsonObject();
	            if(obj.get("_returned").getAsInt() == 0 ? false : true)
				{	
	           
	            JsonObject srcdestIds= obj.getAsJsonObject().get("srcdestId").getAsJsonObject();
	            System.out.println(srcdestIds);
					respBody.add("data",srcdestIds);
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Exist");
				}else {
					respBody.addProperty("data","null");
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Not Exist");
				}
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
	        }
	        catch(HttpClientErrorException e) {
	            JsonObject respBody = new JsonObject();
	            respBody.addProperty("data", "Error");
	            respBody.addProperty("status", "404");
	            System.out.println(e.getMessage());
	            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }   
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
*/
/*	@RequestMapping(method =RequestMethod.GET,value="/getmeteringinfo" )
	private ResponseEntity<String> getMeteringInfo(@RequestParam("userId") String userId)
	{
		  ResponseEntity<String> out = null;
	        HttpHeaders headers = new HttpHeaders();
//	        headers.add("Cache-Control", "no-cache");
//	        headers.add("access-control-allow-origin", config.getRootUrl());
//	        headers.add("access-control-allow-credentials", "true");
	        try {         
	        	
	        	String filter = "{\"_id\":\"" + userId.toLowerCase() + "\"}";
				String url = config.getMongoUrl() + "/credentials/metering?filter=" + filter;
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
	            System.out.println("metering info for admin");
	            System.out.println(uri);
	            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				RestTemplate restTemplate = new RestTemplate();
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				JsonObject respBody = new JsonObject();
	            if(obj.get("_returned").getAsInt() == 0 ? false : true)
				{	
	           
	            
	            System.out.println(obj);
					respBody.add("data",obj);
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Exist");
					
				}else {
					respBody.addProperty("data","null");
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Not Exist");
				}
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
	            
	           
	        	
	        }
	        catch(HttpClientErrorException e) {
	            JsonObject respBody = new JsonObject();
	            respBody.addProperty("data", "Error");
	            respBody.addProperty("status", "404");
	            System.out.println(e.getMessage());
	            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }   
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}*/
}
