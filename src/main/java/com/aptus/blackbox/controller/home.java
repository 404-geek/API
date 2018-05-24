package com.aptus.blackbox.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.index.ScheduleInfo;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.security.ExceptionHandling;
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
public class home extends RESTFetch{
	
	@Autowired
	private Credentials credentials;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private Config config;

	@RequestMapping(value="/login")
	private ResponseEntity<String> login(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
	{
		try {
		
			System.out.println(user);		
			System.out.println("/inside login");		
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
            headers.add("access-control-allow-credentials", "true");
			JsonObject respBody = new JsonObject();
			ResponseEntity<String> ret=null;
			ret = existUser(user,"userInfo");
 			//System.out.println(new Gson().fromJson(existUser(user,"userInfo").getBody(),JsonObject.class).getAsJsonObject().get("code"));
			if(new Gson().fromJson(ret.getBody(),JsonObject.class).getAsJsonObject().get("code").getAsString().equals("200")){
				System.out.println("inside login");
				String url = config.getMongoUrl()+"/credentials/userInfo/"+user;
				System.out.println(url);
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				credentials.setSessionId(user,session.getId());
//				if(!obj.get("password").toString().equals(pass)) {
//					respBody.addProperty("id", user);
//					respBody.addProperty("status", "404");
//					return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers).body(respBody.toString());
//				}
				applicationCredentials.setApplicationCred(user, new ScheduleInfo());
				applicationCredentials.getApplicationCred().get(user).setLastAccessTime(session.getLastAccessedTime());

				if(new Gson().fromJson((existUser(user, "userCredentials").getBody()),JsonObject.class).getAsJsonObject().get("code").getAsString().equals("200"))
					getConnectionIds(session);
								System.out.println(session.getId());
				respBody.addProperty("id", user);
				respBody.addProperty("status", "200");
				System.out.println("inside if");
				//throw new Handling("Some Exception");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				System.out.println(ret.getBody());
				/*respBody.addProperty("id", user);
				respBody.addProperty("status", "404");
				System.out.println(respBody.toString());
				*/
				//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
				return ret;
			}
		}
		
	/*	catch (Exception e) {
			//ExceptionHandling exceptionHandling=new ExceptionHandling()
			// TODO Auto-generated catch block
			System.out.println("inside home handling catch");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.OK).body(null);
			
		}*/
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside login catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		//return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	

	
	
	@RequestMapping(value="/signup")
	private ResponseEntity<String> signup(@RequestParam HashMap<String,String> params)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {			
			System.out.println(params);	
			String userId=params.get("_id");
			JsonObject respBody = new JsonObject();			
        	headers.add("Content-Type", "application/json");
			if(existUser(userId,"userInfo").getBody().contains("200")){			
				System.out.println("User ID Exists");
				respBody.addProperty("id", userId);
				respBody.addProperty("status", "61");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				System.out.println("User ID Not Exists");
				JsonObject body = new JsonObject();
				for(Map.Entry<String, String> entry : params.entrySet()) {
				    body.addProperty(entry.getKey(),entry.getValue());
				}				
				ResponseEntity<String> out = null;
				String url = "";				
				RestTemplate restTemplate = new RestTemplate();
				url = config.getMongoUrl() + "/credentials/userInfo";				
				System.out.println(url);
				HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(),headers);
				out = restTemplate.exchange(url, HttpMethod.POST , httpEntity, String.class);
				if (out.getStatusCode().is2xxSuccessful()) {
					System.out.println("Pushed successfully!");
				}				
				respBody.addProperty("id",userId);
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		} 
		catch(HttpClientErrorException e) {
            JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Error");
            respBody.addProperty("status", "404");
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        }
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	
	
	
	@RequestMapping(value="/update")
	private ResponseEntity<String> update(@RequestParam HashMap<String,String> params,HttpSession session)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Content-Type", "application/json");
		try {
			if(Utilities.isSessionValid(session,credentials)) {
			System.out.println("updating" + params);			
			JsonObject respBody = new JsonObject();
			String userId=params.get("_id").toString();
			String url = config.getMongoUrl()+"/credentials/userInfo/"+ userId;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			JsonObject body = new JsonObject();

			for(Map.Entry<String, String> entry : params.entrySet()) {
			    body.addProperty(entry.getKey(),entry.getValue());
			}
			HttpHeaders header = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(),headers);
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity,String.class);
				
			JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
			System.out.println(session.getId());
			respBody.addProperty("id", userId);
			respBody.addProperty("status", "200");
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers).body(respBody.toString());
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	
	
	/* Input:user_id
	 * Takes user_id as input, checks if user already exists and stores true/false accordingly in credentials.
	 * Return type: void 
	 */
	private ResponseEntity<String> existUser(String userId,String type) {
		ResponseEntity<String> out = null;
		try {
			boolean ret=false;
			credentials.setUserId(userId);
			RestTemplate restTemplate = new RestTemplate();
			//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
			String url;
			url = config.getMongoUrl()+"/credentials/"+type+"?filter=" + filter;
			System.out.println(url);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
            headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println("inside existuser");
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			ret=jobj.get("_returned").getAsInt() == 0 ? false : true;
			if(type.equalsIgnoreCase("usercredentials"))
				credentials.setUserExist(ret);
			
			//System.out.println(ret);
			JsonObject respBody = new JsonObject();
			if(ret)
			{
				 respBody.addProperty("code", "200");
				 respBody.addProperty("message", "User found");
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}
			else
			{
				 respBody.addProperty("code", "404");
				 respBody.addProperty("message", "User not found");
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}
		}

		
		
				
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside exituser catch");
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			//System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
	}

	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	private ResponseEntity<String> getSrcDest(HttpSession session) {
		System.out.println("INSIDE /getsrcdst");
		ResponseEntity<String> s=null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {
			
			System.out.println(session.getId());			
			if(Utilities.isSessionValid(session,credentials)) {
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String url = config.getMongoUrl()+"/credentials/SrcDstlist/srcdestlist";
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
		
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside getsrcdest catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	
	@RequestMapping(value="/filterendpoints")
	private ResponseEntity<String> filterendpoints(HttpSession session)
	{
		System.out.println("INSIDE /filterendpoints");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {			
			if(Utilities.isSessionValid(session,credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId()).setLastAccessTime(session.getLastAccessedTime());
				JsonObject jobj = new JsonObject();
				JsonArray catagory = new JsonArray();
				JsonObject temp = new JsonObject();
				JsonArray endpoints=new JsonArray();
				for(UrlObject obj:credentials.getSrcObj().getDataEndPoints()) {
					if(obj.getCatagory().equalsIgnoreCase("others"))
						endpoints.add(obj.getLabel());
				}
				temp.add("value", endpoints);
				temp.addProperty("name", "others");
				temp.addProperty("key", "Others");
				catagory.add(temp);
		        List<String> list;
		        Gson gson = new Gson();
				for(UrlObject obj:credentials.getSrcObj().getImplicitEndpoints()) {
					temp = new JsonObject();
					ResponseEntity<String> data = token(obj, credentials.getSrcToken(), "filteredEndpoints");					
					list = new ArrayList<String>();
					list = Utilities.check(obj.getData(), gson.fromJson(data.getBody(),JsonElement.class), list);
					temp.add("value", gson.fromJson(gson.toJson(list),JsonArray.class));
					temp.addProperty("name", obj.getCatagory());
					temp.addProperty("key", obj.getLabel());
					catagory.add(temp);
				}
				jobj.add("endpoints", catagory);
				jobj.addProperty("status", "200");
				System.out.println(jobj.toString());
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
	
	
	@RequestMapping(value="/getconnectionids")
	private ResponseEntity<String> getConnectionIds(HttpSession session) {
		System.out.println("INSIDE /getconnectionids");
		String dataSource=null;
		HttpHeaders headers = new HttpHeaders();			
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				ResponseEntity<String> out = null;
				RestTemplate restTemplate = new RestTemplate();
				//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				String url = config.getMongoUrl()+"/credentials/userCredentials/"+credentials.getUserId();
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				header.add("Cache-Control", "no-cache");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
				System.out.println(out.getBody());
				Gson gson=new Gson();								
				JsonObject respBody = new JsonObject();
				respBody.addProperty("status", "200");
				respBody.add("data",gson.fromJson(out.getBody(), JsonElement.class));
				
				ConnObj conObj = new ConnObj();
				JsonElement data = gson.fromJson(out.getBody(), JsonElement.class);
				JsonArray srcdestId = data.getAsJsonObject().get("srcdestId").getAsJsonArray();
				/*for(JsonElement ele:srcdestId) {
					conObj = gson.fromJson(ele, ConnObj.class);
					credentials.setConnectionIds(conObj.getConnectionId(), conObj);
				}
				*/
			    url = config.getMongoUrl()+"/credentials/SrcDstlist/srcdestlist";
			    uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				out  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				respBody.add("images",gson.fromJson(out.getBody(), JsonElement.class));				
				//System.out.println(credentials.getConnectionIds().values());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else {
   				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}			
		}
		catch(HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if(e.getMessage().startsWith("4")) {
	            JsonObject respBody = new JsonObject();
				respBody.addProperty("data", "null");
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
		
}