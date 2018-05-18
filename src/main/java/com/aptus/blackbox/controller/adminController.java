package com.aptus.blackbox.controller;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class adminController {

	@Autowired
	private Config config;
	
	@RequestMapping(method = RequestMethod.GET, value = "/mongoPath")
	private ResponseEntity<String> modifyMongoPath(@RequestParam("mongoPath") String mongoPath) {
		config.setMongoUrl(mongoPath);
		JsonObject obj=new JsonObject();
		obj.addProperty("MongoPath", config.getMongoUrl());
		return ResponseEntity.status(HttpStatus.OK).headers(null).body(obj.toString().toString());
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/addsrcdest")
	private ResponseEntity<String> addSource(@RequestBody String body,@RequestParam("type") String type, @RequestParam("choice") String choice, @RequestParam("sourcename") String srcdestname)
	{
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		headers.add("Content-Type", "application/json");
		String url;
		HttpEntity<?> httpEntity ;
		
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        if(choice.equals("add"))
        	{url=config.getMongoUrl()+"/credentials/"+type;
			httpEntity = new HttpEntity<String>(body,headers);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
        	out = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);
        	 JsonObject respBody = new JsonObject();

     		respBody.addProperty("status", "200");
     		respBody.addProperty("message", type +" successfully added");
     		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        	}
        else if(choice.equals("update"))
        {	url=config.getMongoUrl()+"/credentials/"+type+srcdestname;
        httpEntity = new HttpEntity<String>(body,headers);
        URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
        	out=restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity, String.class);
        	 JsonObject respBody = new JsonObject();

     		respBody.addProperty("status", "200");
     		respBody.addProperty("message", type +" successfully updated");
     		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
        }
	
	@RequestMapping(method =RequestMethod.GET,value="/viewdatasource" )
	private ResponseEntity<String> viewDataSource(@RequestParam("userId") String userId)
	{
		  ResponseEntity<String> out = null;
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Cache-Control", "no-cache");
	        headers.add("access-control-allow-origin", config.getRootUrl());
	        headers.add("access-control-allow-credentials", "true");
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

	@RequestMapping(method =RequestMethod.GET,value="/getmeteringinfo" )
	private ResponseEntity<String> getMeteringInfo(@RequestParam("userId") String userId)
	{
		  ResponseEntity<String> out = null;
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Cache-Control", "no-cache");
	        headers.add("access-control-allow-origin", config.getRootUrl());
	        headers.add("access-control-allow-credentials", "true");
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
	}
}
