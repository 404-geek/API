package com.aptus.blackbox.controller;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*
 * "pagination":[
					{"key":"info.page","to-add":"page","value":"inc"},
 					{"key":"info.page","to-add":"","value":"url"}, 
					{"key":"info.page","to-add":"page","value":"append"}, 
					{"key":"info.page","to-add":"page","value":"self-inc"}, 
 					{"key":"info.page","to-add":"page","value":"self-inc"} 
				]
 */

@Controller
public class home {
	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	@Autowired
	private Credentials credentials;

	@RequestMapping(value="/login")
	private ResponseEntity<String> login(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
	{
		try {
			System.out.println(user);			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
            headers.add("access-control-allow-credentials", "true");
			if(existUser(user)){
				credentials.setSessionId(user,session.getId());
				System.out.println(session.getId());
				JsonObject respBody = new JsonObject();
				respBody.addProperty("id", user);
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				JsonObject respBody = new JsonObject();
				respBody.addProperty("id", user);
				respBody.addProperty("status", "404");
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	
//	@RequestMapping(method = RequestMethod.POST,value="/signup")
//	public ResponseEntity<String> signup(@RequestParam Map<String,String> userInfo)
//	{
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Cache-Control", "no-cache");
//		headers.add("access-control-allow-origin", rootUrl);
//        headers.add("access-control-allow-credentials", "true");
//		try {
//			String userId = userInfo.get("userId");
//			JsonObject respBody = new JsonObject();
//			if(!existUser(userId)) {
//				respBody.addProperty("status", "61");
//				respBody.addProperty("Message", "User already exists");
//				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(null);
//			}
//			
//		}
//		catch(Exception e){
//			
//		}
//	}
	
	
	/* Input:user_id
	 * Takes user_id as input, checks if user already exists and stores true/false accordingly in credentials.
	 * Return type: void 
	 */
	private boolean existUser(String userId) {
		try {
			boolean ret=false;
			ResponseEntity<String> out = null;
			credentials.setUserId(userId);
			RestTemplate restTemplate = new RestTemplate();
			//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
			String url;
			url = mongoUrl+"/credentials/userInfo?filter=" + filter;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
            headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			credentials.setUserExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
			ret = credentials.isUserExist();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.index");
		}
		return false;
	}

	
	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	private ResponseEntity<String> getSrcDest(HttpSession session) {
		ResponseEntity<String> s=null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
        headers.add("access-control-allow-credentials", "true");
		try {
			System.out.println(session.getId());			
			if(Utilities.isSessionValid(session,credentials)) {
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String url = mongoUrl+"/credentials/SrcDstlist/srcdestlist";			 
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				s  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				System.out.println(s.getBody());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(s.getBody().toString());
			}
			else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	
	@RequestMapping(value="/filterendpoints")
	private ResponseEntity<String> filterendpoints(HttpSession session)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		try {			
			if(Utilities.isSessionValid(session,credentials)) {
				JsonObject jobj = new JsonObject();
				JsonArray endpoints=new JsonArray();
				for(UrlObject obj:credentials.getSrcObj().getEndPoints()) {
					endpoints.add(obj.getLabel());
				}
				jobj.add("endpoints", endpoints);
				jobj.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobj.toString());
			}
			else
			{
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
		
}