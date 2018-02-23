package com.aptus.blackbox.controller;

import java.net.URI;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.Parser;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
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
	private SrcObject srcObj;
	private DestObject destObj;

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
//	@RequestMapping(value="/signup")
//	public void signup(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
//	{
//		System.out.println(session.getId());
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
			url = mongoUrl+"/credentials/userCredentials?filter=" + filter;
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

	/*
	 * input:  type(source/destination) and its name
	 * Parses its configuration file and stores it in credentials
	 * output:void
	 */
	private void srcDestId(String type, String srcdestId) {
		//change return type to void
		if (type.equalsIgnoreCase("source")) {
			srcObj = new Parser("source",srcdestId.toUpperCase(),mongoUrl).getSrcProp();
			credentials.setSrcObj(srcObj);
			credentials.setSrcName(srcdestId.toLowerCase());
			credentials.setSrcValid(false);
			
		} else {
			destObj = new Parser("destination",srcdestId.toUpperCase(),mongoUrl).getDestProp();
			credentials.setDestObj(destObj);
			credentials.setDestName(srcdestId.toLowerCase());
			credentials.setDestValid(false);
		}
		return;
	}

	/*
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/validate")
	private ResponseEntity<String> verifyUser(@RequestParam("type") String type,@RequestParam("srcdestId") String srcdestId,HttpSession session) {
		ResponseEntity<String> out = null;
		int res = 0;
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				srcDestId(type,srcdestId);
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String filter;
				String url;
				if (type.equalsIgnoreCase("source"))
					name = credentials.getSrcName();
				else
					name = credentials.getDestName();
				filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase()+"_"+name.toLowerCase() + "\"}";
				
				url = mongoUrl+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
				 
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders headers = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				headers.add("Cache-Control", "no-cache");
				headers.add("access-control-allow-origin", rootUrl);
				headers.add("access-control-allow-credentials", "true");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				System.out.println(out.getBody());
				JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
				JsonObject jobj = jelem.getAsJsonObject();
				if (type.equalsIgnoreCase("source")) {
					credentials.setUsrSrcExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
					System.out.println(url+" : "+credentials.isUsrSrcExist());
				}
				else {
					credentials.setUsrDestExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
					System.out.println(url+" : "+credentials.isUsrDestExist());
				}
				out = initialiser(type);
			}
			else {
				System.out.println("Session expired!");
				HttpHeaders headers = new HttpHeaders();
				String url=homeUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.verifyuser");
		}
		return out;
	}

	private ResponseEntity<String> initialiser(String type) {
		//add destination fetch and validation
		ResponseEntity<String> out = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {
				if(type.equalsIgnoreCase("source")) {
					fetchSrcdestCred(type);
					System.out.println(srcObj+" "+credentials);
					out = Utilities.token(srcObj.getValidateCredentials(),credentials.getSrcToken());
					if (out.getStatusCode().is2xxSuccessful()) {
						System.out.println(type + "tick");
						Utilities.valid();
						credentials.setSrcValid(true);
						headers = new HttpHeaders();
						String url=homeUrl;
						headers.setLocation(URI.create(url+"/close.html"));
						headers.add("Cache-Control", "no-cache");
						headers.add("access-control-allow-origin", rootUrl);
						headers.add("access-control-allow-credentials", "true");
						return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
						//return  new ResponseEntity<String>("valid", null, HttpStatus.CREATED);
						// tick
					}
					else {
						String url =  baseUrl+"/authsource";
						System.out.println(url);						
						headers.setLocation(URI.create(url));
						out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
					}
				}
				else {
					String url =  baseUrl+"/authdestination";
					System.out.println(url);
					headers.setLocation(URI.create(url));
					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
				}
				
			} 
			else {
				String name;
				if (type.equalsIgnoreCase("source"))
					name = credentials.getSrcName();
				else
					name = credentials.getDestName();
				String url = baseUrl+"/auth" + type;
				System.out.println(url);
				headers.setLocation(URI.create(url));
				out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.init");
		}
		return out;
	}

	private void fetchSrcdestCred(String type) {
		ResponseEntity<String> out = null;
		int res = 0;
		String userid = credentials.getUserId(), appId;
		try {
			if (type.equalsIgnoreCase("source"))
				appId = credentials.getSrcName();
			else
				appId = credentials.getDestName();
			RestTemplate restTemplate = new RestTemplate();
			String url = mongoUrl+"/credentials/" + type + "Credentials/" + userid.toLowerCase()+"_"+appId.toLowerCase();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
            headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(URI.create(url), HttpMethod.GET, httpEntity, String.class);
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			for(JsonElement ob:jobj.get("credentials").getAsJsonArray()) {
				String key=ob.getAsJsonObject().get("key").toString(),
						value=ob.getAsJsonObject().get("value").toString();
				key=key.substring(1, key.length()-1);
				value=value.substring(1, value.length()-1);
				credentials.setSrcToken(key,value);
			}
			System.out.println(credentials.getSrcToken().keySet()+" : "+credentials.getSrcToken().values());
			//Add destination fetching
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.fetch");
		}
	}
	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	private ResponseEntity<String> getSrcDest(HttpSession session) {
		ResponseEntity<String> s=null;
		try {
			System.out.println(session.getId());
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
            headers.add("access-control-allow-credentials", "true");
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
				headers = new HttpHeaders();
				String url=homeUrl;
				headers.setLocation(URI.create(url));
				s = new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
		
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	@RequestMapping(method = RequestMethod.GET, value = "/isvalid")
	private ResponseEntity<String> isValid(@RequestParam("type") String type,
			@RequestParam("srcdestId") String srcDestId, HttpSession session) {
		boolean isvalid = false;
		if(type.equals("source")){
			if(!credentials.getSrcName().equalsIgnoreCase(srcDestId)) {
				credentials.setSrcValid(false);
			}
			isvalid=credentials.isSrcValid();
		}
		else if(type.equals("destination")) {
			if(!credentials.getDestName().equalsIgnoreCase(srcDestId)) {
				credentials.setDestValid(false);
			}
			isvalid=credentials.isDestValid();
		}
		JsonObject jobject = new JsonObject();
		jobject.addProperty("isvalid",isvalid);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		return ResponseEntity.status(HttpStatus.OK).body(jobject.toString());
	}

}