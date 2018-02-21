package com.aptus.blackbox.controller;

import java.net.URI;
import java.nio.charset.Charset;

import javax.servlet.http.HttpSession;

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
import org.springframework.web.client.HttpClientErrorException;
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
	private String mongoUrl;
	
	public home(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	@Autowired
	private Credentials credentials;
	private SrcObject srcObj;
	private DestObject destObj;

	@RequestMapping(value="/login")
	private String login(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
	{
		credentials.setSessionId(user,session.getId());
		System.out.println(session.getId());
		existUser(user);
		return Utilities.getsession(session, credentials);
		//store in credentials
	}
//	@RequestMapping(value="/signup")
//	public void signup(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
//	{
//		System.out.println(session.getId());
//	}
	@RequestMapping(value="/getdatasources")
	private String dataSources(@RequestParam("userId") String user) {
		String dataSource=null;
		try {
			ResponseEntity<String> out = null;
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			String url = "http://"+mongoUrl+"/credentials/userCredentials/"+credentials.getUserId();
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println(out.getBody());
			dataSource = "{ \"data\":"+out.getBody().toString()+",\"status\":200}";
			System.out.println(dataSource);
			return dataSource;
		}
		catch(HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if(e.getMessage().startsWith("4")) {
				return "{ \"data\":null,\"status\":404}";
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return dataSource;
	}
	
	/* Input:user_id
	 * Takes user_id as input, checks if user already exists and stores true/false accordingly in credentials.
	 * Return type: void 
	 */
	private void existUser(String userId) {
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
			headers.add("Access-Control-Allow-Origin", "*");
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
		return;
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
		} else {
			destObj = new Parser("destination",srcdestId.toUpperCase(),mongoUrl).getDestProp();
			credentials.setDestObj(destObj);
			credentials.setDestName(srcdestId.toLowerCase());
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
			if(Utilities.getsession(session,credentials)!=null) {
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
				
				url = "http://"+mongoUrl+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
				 
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders headers = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				headers.add("Cache-Control", "no-cache");
				headers.add("Access-Control-Allow-Origin", "*");
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
				String url="http://localhost:8080/login";
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
		ResponseEntity<String> out = null;
		try {
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {
				fetchSrcdestCred(type);
				out = Utilities.token(srcObj.getValidateCredentials(),
						type.equalsIgnoreCase("source") ? credentials.getSrcToken() : credentials.getDestToken());
				if (out.getStatusCode().is2xxSuccessful()) {
					System.out.println(type + "tick");
					Utilities.valid();
					return  new ResponseEntity<String>("valid", null, HttpStatus.CREATED);
					// tick
				} 
				//add destination fetch and validation
				else {
					String name;
					if (type.equalsIgnoreCase("source"))
						name = credentials.getSrcName();
					else
						name = credentials.getDestName();
					String url = "http://localhost:8080/auth" + type;
					System.out.println(url);

					HttpHeaders headers = new HttpHeaders();
					headers = new HttpHeaders();
					headers.add("Cache-Control", "no-cache");
					headers.add("Access-Control-Allow-Origin", "*");
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
				String url = "http://localhost:8080/auth" + type;
				System.out.println(url);

				HttpHeaders headers = new HttpHeaders();
				headers = new HttpHeaders();
				headers.add("Cache-Control", "no-cache");
				headers.add("Access-Control-Allow-Origin", "*");
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
			String url = "http://"+mongoUrl+"/credentials/" + type + "Credentials/" + userid.toLowerCase()+"_"+appId.toLowerCase();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("Access-Control-Allow-Origin", "*");
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
	private String getSrcDest(HttpSession session) {
		String s=null;
		try {
			if(Utilities.getsession(session,credentials)!=null) {
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String url = "http://"+mongoUrl+"/credentials/SrcDstlist";			 
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders headers = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				headers.add("Cache-Control", "no-cache");
				headers.add("Access-Control-Allow-Origin", "*");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				s  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody();
			}
			else {
				return "redirect://";
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return s;
	}

}
