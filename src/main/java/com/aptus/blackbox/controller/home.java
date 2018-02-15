package com.aptus.blackbox.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.Parser;
import com.aptus.blackbox.utils.Utilities;
import com.google.common.net.UrlEscapers;
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
	private String mongoUrl;
	
	public home(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	@Autowired
	private Credentials credentials;
	private SrcObject srcObj;
	private DestObject destObj;

	@RequestMapping(value = "/{userId}")
	public Object index(@PathVariable String userId) {
		//Change return to void
		try {
			ResponseEntity<String> out = null;
			credentials.setUserId(userId);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
			String url;
			url = "http://"+mongoUrl+"/credentials/userCredentials?filter=" + filter;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			credentials.setUserExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.index");
		}
		return "index";
	}

	@RequestMapping(value = "/{type}/{srcdestId}")
	public Object source(@PathVariable String type, @PathVariable String srcdestId) {
		//change return type to void

		if (type.equalsIgnoreCase("source")) {
			srcObj = new Parser("source/" + srcdestId.toUpperCase()).getSrcProp();
			credentials.setSrcObj(srcObj);
			credentials.setSrcName(srcdestId.toLowerCase());
		} else {
			destObj = new Parser("destination/" + srcdestId.toUpperCase()).getDestProp();
			credentials.setDestObj(destObj);
			credentials.setDestName(srcdestId.toLowerCase());
		}
		return "index";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/validate")
	public ResponseEntity<String> verifyUser(@RequestParam("type") String type) {
		ResponseEntity<String> out = null;
		int res = 0;
		try {
			String name;
			RestTemplate restTemplate = new RestTemplate();
			String filter;
			String url;
			if (type.equalsIgnoreCase("source"))
				name = credentials.getSrcName();
			else
				name = credentials.getDestName();
			filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase()+"_"+name.toLowerCase() + "\"}";
			
			url = "http://"+mongoUrl+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
			 
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
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

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.verifyuser");
		}
		return out;
	}

	private ResponseEntity<String> initialiser(String type) {
		ResponseEntity<String> out = null;
		try {
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {
				fetchSourceCred(type);
				out = Utilities.token(srcObj.getValidateCredentials(),
						type.equalsIgnoreCase("source") ? credentials.getSrcToken() : credentials.getDestToken());
				if (out.getStatusCode().is2xxSuccessful()) {
					System.out.println(type + "tick");
					Utilities.valid();
					return  new ResponseEntity<String>("valid", null, HttpStatus.CREATED);
					// tick
				} 
				else {
					String name;
					if (type.equalsIgnoreCase("source"))
						name = credentials.getSrcName();
					else
						name = credentials.getDestName();
					String url = "http://localhost:8080/auth" + type + "/" + name + "";
					System.out.println(url);

					HttpHeaders headers = new HttpHeaders();
					headers = new HttpHeaders();
					headers.add("Cache-Control", "no-cache");
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
				String url = "http://localhost:8080/auth" + type + "/" + name + "";
				System.out.println(url);

				HttpHeaders headers = new HttpHeaders();
				headers = new HttpHeaders();
				headers.add("Cache-Control", "no-cache");
				headers.setLocation(URI.create(url));
				out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.init");
		}
		return out;
	}

	private void fetchSourceCred(String type) {
		ResponseEntity<String> out = null;
		int res = 0;
		String userid = credentials.getUserId(), appId;
		try {
			if (type.equalsIgnoreCase("source"))
				appId = credentials.getSrcName();
			else
				appId = credentials.getDestName();
			RestTemplate restTemplate = new RestTemplate();
			String url = "http://"+mongoUrl+"/credentials/" + type + "Credentials/" + userid.toLowerCase()+"_"+appId.toLowerCase();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
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
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.fetch");
		}
	}
	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	public String getSrcDest() {
		String s=null;
		try {
			String name;
			RestTemplate restTemplate = new RestTemplate();
			String url = "http://"+mongoUrl+"/credentials/SrcDstlist";			 
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			s  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return s;
	}

}
