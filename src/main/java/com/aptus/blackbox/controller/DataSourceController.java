package com.aptus.blackbox.controller;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.BlackBoxReloadedApp;
import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataInterfaces.DestinationConfigDAO;
import com.aptus.blackbox.dataInterfaces.SourceConfigDAO;
import com.aptus.blackbox.dataInterfaces.SrcDestCredentialsDAO;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.datamodels.SrcDestCredentials;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.event.Socket;
import com.aptus.blackbox.index.Parser;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.Endpoint;
import com.aptus.blackbox.security.ExceptionHandling;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
@RestController
public class DataSourceController extends RESTFetch {

	@Autowired
	private Credentials credentials;	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private Config config;
	@Autowired
	private SourceConfigDAO sourceConfigDAO;
	@Autowired
	private DestinationConfigDAO destinationConfigDAO;
	@Autowired
	private SrcDestCredentialsDAO srcDestCredentialsDAO;
	@Autowired
	private ApplicationContext Context;
	final Logger logger = LogManager.getLogger(BlackBoxReloadedApp.class.getPackage());

	
//	private SrcObject srcObj;
//	private DestObject destObj;
	/*
	 * input:  type(source/destination) and its na
					credentials.setDestObj(parse.getDestProp());
					credentials.setCurrDestName(srcdestId.toLowerCase());
					credentials.setCurrDestValid(false);
				me
	 * Parses its configuration file and stores it in credentials
	 * output:void
	 */
	@RequestMapping(value ="/src",method= RequestMethod.POST)
	public void addSourceConfig(@RequestBody String conf) {
		SourceConfig conf1 = new Gson().fromJson(conf, SourceConfig.class);
		sourceConfigDAO.createSourceConfig(conf1);
	}
	
	@RequestMapping(value ="/dest",method= RequestMethod.POST)
	public void addDestinationConfig(@RequestBody String conf) {
		DestinationConfig conf1 = new Gson().fromJson(conf, DestinationConfig.class);
		destinationConfigDAO.createDestinationConfig(conf1);
	}
	
	public void srcDestId(String type, String srcdestId) {
		//change return type to void
		try {
			srcdestId = srcdestId.toUpperCase();
			System.out.println(type+ " "+srcdestId);
			Parser parse = Context.getBean(Parser.class);
			if (type.equalsIgnoreCase("source")) {	
				    System.out.println("sssssssssssssss :"+sourceConfigDAO.getSourceConfig(srcdestId));
				if(parse.parsingJson("source",srcdestId.toUpperCase(),config.getMongoUrl()).getStatusCode().is2xxSuccessful());{
					credentials.setSrcObj(parse.getSrcProp());
					credentials.setCurrSrcName(srcdestId.toLowerCase());
					credentials.setCurrSrcValid(false);
				}
				
			} else {			
				if(parse.parsingJson("destination",srcdestId.toUpperCase(),config.getMongoUrl()).getStatusCode().is2xxSuccessful());{
					credentials.setDestObj(parse.getDestProp());
					credentials.setCurrDestName(srcdestId.toLowerCase());
					credentials.setCurrDestValid(false);
				}
			}
		} catch (BeansException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	public void srcDestId1(String type, String srcdestId) {
		//change return type to void
		
			try {
				srcdestId = srcdestId.toUpperCase();
				System.out.println(type+ " "+srcdestId);
				
				if (type.equalsIgnoreCase("source")) {	
					    SourceConfig srcobj=  sourceConfigDAO.getSourceConfig(srcdestId);
					    //TODO check if srcObj is null
						credentials.setSrcObj(srcobj);
						credentials.setCurrSrcName(srcdestId.toLowerCase());
						credentials.setCurrSrcValid(false);

				} else {			
						DestinationConfig destObj = destinationConfigDAO.getDestinationConfig(srcdestId);
						//TODO check if destObj is null
						credentials.setDestObj(destObj);
						credentials.setCurrDestName(srcdestId.toLowerCase());
						credentials.setCurrDestValid(false);
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		return;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/validate1")
	private ResponseEntity<String> verifyUser1(@RequestParam("type") String type,@RequestParam("srcdestId") String srcdestId,HttpSession session,
			@RequestParam(value ="database_name",required=false) String database_name,
			@RequestParam(value ="db_username",required=false) String db_username,
			@RequestParam(value ="db_password",required=false) String db_password,
			@RequestParam(value ="server_host",required=false) String server_host,
			@RequestParam(value ="server_port",required=false) String server_port){
		
		
		ResponseEntity<String> out = null;
		System.out.println("inside validate");
		
		int res = 0;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			System.out.println("inside validate function");
			
			if(session.getId()==applicationCredentials.getSessionId(credentials.getUserId())) {
				
				credentials.setCurrConnId(null);///check here
				System.out.println(srcdestId);
				srcDestId1(type,srcdestId);
				String credentialId = credentials.getUserId()+"_";
				/*
				 * 	Check and sets if user src/dest exist						
				 */
				if (type.equalsIgnoreCase("source")) {
					credentialId += credentials.getCurrSrcName();
					boolean usrSrcExist = srcDestCredentialsDAO.srcDestCredentialsExist(credentialId.toLowerCase(), Constants.COLLECTION_SOURCECREDENTIALS);
					credentials.setUsrSrcExist(usrSrcExist);
					System.out.println("User Source Exist : "+credentials.isUsrSrcExist());
				}
				else {
					credentialId += credentials.getCurrDestName();
					boolean usrDestExist = srcDestCredentialsDAO.srcDestCredentialsExist(credentialId.toLowerCase(), Constants.COLLECTION_DESTINATIONCREDENTIALS);
					credentials.setUsrDestExist(usrDestExist);
					System.out.println("User Destination Exist : "+credentials.isUsrDestExist());
				}
				return initialiser(type,database_name,db_username,db_password,server_host,server_port);
			}
			else {
				System.out.println("Ses"
						+ "sion expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		
		}		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.verifyuser");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	
	/*
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/validate")
	private ResponseEntity<String> verifyUser(@RequestParam("type") String type,@RequestParam("srcdestId") String srcdestId,HttpSession session,
			@RequestParam(value ="database_name",required=false) String database_name,
			@RequestParam(value ="db_username",required=false) String db_username,
			@RequestParam(value ="db_password",required=false) String db_password,
			@RequestParam(value ="server_host",required=false) String server_host,
			@RequestParam(value ="server_port",required=false) String server_port){
		
		
		ResponseEntity<String> out = null;
		System.out.println("inside validate");
		
		int res = 0;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			System.out.println("inside validate function");
			
			if(session.getId()==applicationCredentials.getSessionId(credentials.getUserId())) {
				
				credentials.setCurrConnId(null);///check here
				System.out.println(srcdestId);
				srcDestId(type,srcdestId);
				
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String filter;
				String url;
				
				if (type.equalsIgnoreCase("source")) {
					name = credentials.getCurrSrcName();
					filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase()+"_"+name.toLowerCase() + "\"}";
					
				}					
				else {
					name = credentials.getCurrDestName();
					filter = "{\"_id\":{\"$regex\":\".*"+credentials.getUserId().toLowerCase()+"_"+name.toLowerCase() + ".*\"}}";
				}
				
				url = config.getMongoUrl()+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
				System.out.println("************************url********"+ url);
				System.out.println(type+" : "+url);
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();				
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				System.out.println(out.getBody());
				JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
				JsonObject jobj = jelem.getAsJsonObject();
				System.out.println("datasourceontroller /validate"+jobj.get("_returned").getAsInt());
				if (type.equalsIgnoreCase("source")) {
					credentials.setUsrSrcExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
					System.out.println(url+" : "+credentials.isUsrSrcExist());
					
				}
				else {
					
					credentials.setUsrDestExist(jobj.get("_returned").getAsInt() == 0 ? false : true);
					System.out.println(url+" : "+credentials.isUsrDestExist());
					
				}
				
				return initialiser(type,database_name,db_username,db_password,server_host,server_port);
			}
			else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		
		}		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.verifyuser");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	private ResponseEntity<String> initialiser1(String type,String database_name,String db_username,String db_password,String server_host,String server_port) {
		//add destination fetch and validation
		System.out.println("inside initialiser function");
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		

		headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		System.out.println("srcExist:"+credentials.isUsrSrcExist() +"\ndestExist:"+ credentials.isUsrDestExist()+" type:"+type);

		try {			
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {				
				if(type.equalsIgnoreCase("source")) {
					credentials.setCurrSrcValid(false);
					fetchSrcCred1();
					
					
					System.out.println(type+" credentials already exist");
					System.out.println(credentials.getSrcObj()+" "+credentials);
					out = Utilities.token(credentials.getSrcObj().getValidateCredentials(),credentials.getSrcToken(),credentials.getUserId()+"DataSourceController.initialiser");
					//System.out.println("OUt:"+out);
					System.out.println("Out status code :"+out.getStatusCode());
					if (out.getStatusCode().is2xxSuccessful()) {
						System.out.println(type + "tick");
						Utilities.valid();
						credentials.setCurrSrcValid(true);
						String url="/close.html";
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						//return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).headers(headers).body(null);
						return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
						//return  new ResponseEntity<String>("valid", null, HttpStatus.CREATED);
						// tick
					}
					else {
						String url =  "/authsource";
						System.out.println("/authsource called");	
						System.out.println(url);	
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
					}
				}
				else {
					logger.info("pos 1");
					credentials.setCurrDestValid(false);
					out = Context.getBean(DataController.class).destination( database_name, db_username, db_password, server_host, server_port);
//					String url =  "/authdestination?"+
//							"database_name="+database_name+
//							"&db_username="+db_username+
//							"&db_password="+db_password+
//							"&server_host="+server_host+
//							"&server_port="+server_port;
//					System.out.println(url);	
//					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
//					headers.setLocation(uri);
//					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);					
				}				
			} 
			else {
				System.out.println("New credentials fetch");
				
				String url;
				if (type.equalsIgnoreCase("source")) {
					url = "/authsource";
					System.out.println(url);
					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
					headers.setLocation(uri);
					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
				}					
				else {
					logger.info("pos 2");
					out = Context.getBean(DataController.class).destination( database_name, db_username, db_password, server_host, server_port);
				}
//					url =  "/authdestination?"+
//							"database_name="+database_name+
//							"&db_username="+db_username+
//							"&db_password="+db_password+
//							"&server_host="+server_host+
//							"&server_port="+server_port;
				
			}
			return out;
			
		} 
		

		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside initialiser catch");
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			//System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.init");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	private ResponseEntity<String> initialiser(String type,String database_name,String db_username,String db_password,String server_host,String server_port) {
		//add destination fetch and validation
		System.out.println("inside initialiser function");
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		

		headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		System.out.println("srcExist:"+credentials.isUsrSrcExist() +"\ndestExist:"+ credentials.isUsrDestExist()+" type:"+type);

		try {			
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {				
				if(type.equalsIgnoreCase("source")) {
					credentials.setCurrSrcValid(false);
					fetchSrcCred();
					
					
					System.out.println(type+" credentials already exist");
					System.out.println(credentials.getSrcObj()+" "+credentials);
					out = Utilities.token(credentials.getSrcObj().getValidateCredentials(),credentials.getSrcToken(),credentials.getUserId()+"DataSourceController.initialiser");
					//System.out.println("OUt:"+out);
					System.out.println("Out status code :"+out.getStatusCode());
					if (out.getStatusCode().is2xxSuccessful()) {
						System.out.println(type + "tick");
						Utilities.valid();
						credentials.setCurrSrcValid(true);
						String url="/close.html";
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						//return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).headers(headers).body(null);
						return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
						//return  new ResponseEntity<String>("valid", null, HttpStatus.CREATED);
						// tick
					}
					else {
						String url =  "/authsource";
						System.out.println("/authsource called");	
						System.out.println(url);	
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
					}
				}
				else {
					logger.info("pos 1");
					credentials.setCurrDestValid(false);
					out = Context.getBean(DataController.class).destination( database_name, db_username, db_password, server_host, server_port);
//					String url =  "/authdestination?"+
//							"database_name="+database_name+
//							"&db_username="+db_username+
//							"&db_password="+db_password+
//							"&server_host="+server_host+
//							"&server_port="+server_port;
//					System.out.println(url);	
//					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
//					headers.setLocation(uri);
//					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);					
				}				
			} 
			else {
				System.out.println("New credentials fetch");
				
				String url;
				if (type.equalsIgnoreCase("source")) {
					url = "/authsource";
					System.out.println(url);
					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
					headers.setLocation(uri);
					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
				}					
				else {
					logger.info("pos 2");
					out = Context.getBean(DataController.class).destination( database_name, db_username, db_password, server_host, server_port);
				}
//					url =  "/authdestination?"+
//							"database_name="+database_name+
//							"&db_username="+db_username+
//							"&db_password="+db_password+
//							"&server_host="+server_host+
//							"&server_port="+server_port;
				
			}
			return out;
			
		} 
		

		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside initialiser catch");
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			//System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.init");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	private void fetchSrcCred1() {
		ResponseEntity<String> out = null;
		int res = 0;
		System.out.println("inside fetchSrcCred");
		String userid = credentials.getUserId(), appId;
		try {		
			String credentialId = userid.toLowerCase()+"_"+credentials.getCurrSrcName().toLowerCase();
		
			SrcDestCredentials srcDestCredential = srcDestCredentialsDAO.
					getCredentials(credentialId, Constants.COLLECTION_SOURCECREDENTIALS);
			for(Map<String,String> hm:srcDestCredential.getCredentials()) {
				for(Map.Entry<String, String> map : hm.entrySet())
					credentials.addSrcToken(map.getKey(), map.getValue());
			}
			System.out.println("Keys: "+credentials.getSrcToken().keySet()+" \nValues: "+credentials.getSrcToken().values());
		} 
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside fetchSrcCred catch");
			e.getStatusCode();
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.fetch");
		}
	}
	private void fetchSrcCred() {
		ResponseEntity<String> out = null;
		int res = 0;
		System.out.println("inside fetchSrcCred");
		String userid = credentials.getUserId(), appId;
		try {
			appId = credentials.getCurrSrcName();
			RestTemplate restTemplate = new RestTemplate();
			String url = config.getMongoUrl()+"/credentials/sourceCredentials/" + userid.toLowerCase()+"_"+appId.toLowerCase();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
            headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			for(JsonElement ob:jobj.get("credentials").getAsJsonArray()) {
				String key=ob.getAsJsonObject().get("key").toString(),
						value=ob.getAsJsonObject().get("value").toString();
				key=key.substring(1, key.length()-1);
				value=value.substring(1, value.length()-1);
				credentials.addSrcToken(key,value);
			}
			System.out.println("Keys: "+credentials.getSrcToken().keySet()+" \nValues: "+credentials.getSrcToken().values());
		} 
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside fetchSrcCred catch");
			ResponseEntity<String> out1 = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.fetch");
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/isvalid")
	private ResponseEntity<String> isValid(@RequestParam("type") String type,
			@RequestParam("srcdestId") String srcDestId, HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				boolean isvalid = false;
				System.out.println(type+" "+srcDestId);
				if(type.equals("source")){
					if(!credentials.getCurrSrcName().equalsIgnoreCase(srcDestId)) {
						isvalid=false;
					}
					else {
						ResponseEntity<String> out = Utilities.token(credentials.getSrcObj().getValidateCredentials(), credentials.getSrcToken(), "isvalid");
						isvalid = out.getStatusCode().is2xxSuccessful();
						Context.getBean(SourceController.class).saveValues(out);
					}
					//isvalid=credentials.isCurrSrcValid();
					credentials.setCurrSrcValid(isvalid);
				}
				else if(type.equals("destination")) {
					if(!credentials.getCurrDestName().equalsIgnoreCase(srcDestId)) {
						isvalid=false;
					}
					else
						isvalid=Context.getBean(DataController.class).checkDB(credentials.getDestToken().get("database_name"), credentials.getDestToken(), credentials.getDestObj()).get("code").getAsString().equalsIgnoreCase("200");
					//isvalid=credentials.isCurrDestValid();
					credentials.setCurrDestValid(isvalid);
				}
				JsonObject jobject = new JsonObject();
				jobject.addProperty("isvalid",isvalid);
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobject.toString());
			}
			else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObject jobject = new JsonObject();
		jobject.addProperty("isvalid",false);
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobject.toString());
	}
	@RequestMapping(method = RequestMethod.GET, value = "/deletedatasource")
    private ResponseEntity<String> deleteDataSource(HttpSession session, @RequestParam("connId") String connId) {
        ResponseEntity<String> out = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        try {         
            HttpEntity<?> httpEntity;
            if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
                String url = config.getMongoUrl() + "/credentials/userCredentials/" + credentials.getUserId();
                System.out.println("DeleteDataSource");
                System.out.println(url);
                JsonObject obj1 = new JsonObject();
                obj1.addProperty("connectionId", connId);
                JsonObject obj2 = new JsonObject();
                obj2.add("srcdestId", obj1);
                JsonObject body = new JsonObject();
                body.add("$pull", obj2);
                headers.add("Content-Type", "application/json");
                System.out.println("68542168521"+body.toString());
                httpEntity = new HttpEntity<Object>(body.toString(), headers);
                RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
                URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
                out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity, String.class);
                if (out.getStatusCode().is2xxSuccessful()) { 
                	
                	applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));
                	
                	if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
                		if(applicationCredentials.getApplicationCred().get(credentials
                				.getUserId()).getSchedulingObjects().get(connId)!=null) {
                			applicationEventPublisher.publishEvent(new InterruptThread(applicationCredentials.getApplicationCred().get(credentials
                    				.getUserId()).getSchedulingObjects().get(connId).getThread()
                					, false, credentials.getUserId(), credentials.getCurrConnId().getConnectionId()));
                			url = config.getMongoUrl() + "/credentials/scheduledStatus/" + credentials.getUserId();
                            System.out.println("Delete scheduled DataSource");
                            System.out.println(url);
                            obj1 = new JsonObject();
                            obj1.add(connId,null);
                            obj2 = new JsonObject();
                            obj2.add("$unset", obj1);
                            headers.add("Content-Type", "application/json");
                            System.out.println("Datasourcecontroller deletedatasource"+obj2.toString());
                            httpEntity = new HttpEntity<Object>(obj2.toString(), headers);
                            restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
                            uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
                            out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity, String.class);
                		}
                	}
                    System.out.println(connId + "***********Deleted!!!!**************");
                    JsonObject respBody = new JsonObject();
                    respBody.addProperty("data", "Sucessfully Deleted");
                    respBody.addProperty("status", "200");
                    return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
                }
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/view")
	private ResponseEntity<String> view(@RequestParam(value = "filteredEndpoints", required=false) String filteredEndpoints,
			@RequestParam(value="scheduled") String schedule,
			@RequestParam(value="period",required=false) String period,
			@RequestParam(value="destination") String destination,
			HttpSession session){
		try {
			if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
				//filteredEndpoints = "{ \"others\": { \"orgId\": true ,\"ticket_id\": true}}";
				List<String> downloadDest = new ArrayList<String>(){{
					add("csv");
					add("xml");
					add("json");
				}};
				String conId;
				JsonObject respBody = new JsonObject();
				if((credentials.getCurrDestName()!=null)&&(!credentials.getCurrDestName().equalsIgnoreCase(destination))) {
					respBody.addProperty("message", "not matching dest name");
					respBody.addProperty("status", "401");
				}
				else if((credentials.getCurrDestName()==null)&&(!downloadDest.contains(destination))) {
					respBody.addProperty("message", "incorrect dest name");
					respBody.addProperty("status", "401");
				}
				else {
					conId = credentials.getUserId() + "_" + credentials.getCurrSrcName() + "_" + destination.toLowerCase() + "_"
							+ String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
					Gson gson = new Gson();
//					String schedule = filteredEndpoints.get("scheduled");
//					String period = filteredEndpoints.get("period");
//					String schedule = "true";
//					String period = "120";
					JsonArray temp2,endpoints = new JsonArray();
					JsonObject temp1;
					JsonElement temp = gson.fromJson(filteredEndpoints, JsonElement.class)
							.getAsJsonObject();
					for(Entry<String, JsonElement> e : temp.getAsJsonObject().entrySet()) {
						temp1  = new JsonObject();
						temp2 = new JsonArray();
						temp1.addProperty("name",e.getKey());
						temp1.addProperty("key",e.getKey());
						
						for(Entry<String, JsonElement> e1 : e.getValue().getAsJsonObject().entrySet()) {
							if(e1.getValue().getAsString().equalsIgnoreCase("true"))
								temp2.add(e1.getKey());
						}
						temp1.add("value", temp2);
						endpoints.add(temp1);
					}
					System.out.println(endpoints);
//					JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
//							.getAsJsonObject().get("endpoints").getAsJsonArray();
					ConnObj currobj = new ConnObj();
					Endpoint endpoint = new Endpoint();
					for (JsonElement obj : endpoints) {
						endpoint = gson.fromJson(obj, Endpoint.class);
						currobj.setEndPoints(endpoint);
					}
					currobj.setConnectionId(conId);
					currobj.setSourceName(credentials.getCurrSrcName());
					currobj.setDestName(destination.toLowerCase());
					currobj.setPeriod(Integer.parseInt(period)*1000);
					currobj.setScheduled(schedule);
					
					credentials.setCurrConnId(currobj);
					
					ResponseEntity<String> str = Context.getBean(DataController.class).selectAction("view", session);
					HttpHeaders headers = new HttpHeaders();
					headers.add("Cache-Control", "no-cache");
					headers.add("access-control-allow-origin", config.getRootUrl());
					headers.add("access-control-allow-credentials", "true");
					return ResponseEntity.status(HttpStatus.OK).headers(headers).body(str.getBody());
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/createdatasource")
	private ResponseEntity<String> createDataSource(@RequestParam(value = "filteredEndpoints", required=false) String filteredEndpoints,
			@RequestParam(value="scheduled") String schedule,
			@RequestParam(value="period",required=false) String period,
			@RequestParam(value="destination") String destination,
			@RequestParam(value="choice") String choice,
			HttpSession session) {
		ResponseEntity<String> ret = null;
		try {
//			filteredEndpoints = new HashMap<>();
//			filteredEndpoints.put("filteredendpoints", "{\"endpoints\": [{\n" + 
//					"		\"name\":\"module_api_name\",\n" + 
//					"		\"key\":\"modules\",\n" +
//					"		\"value\": [\n" + 
//					"			\"Accounts\"\n" + 
//					"		]}]}");
			//filteredEndpoints = "{ \"others\": { \"feed\": true ,\"albums\": true}}";
			System.out.println(filteredEndpoints.getClass());
			System.out.println(filteredEndpoints + " ");
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
			headers.add("access-control-allow-credentials", "true");
			if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
				// if(validateCredentials==null||endPoints==null||refreshToken==null) {
				// init();
				// }
				List<String> downloadDest = new ArrayList<String>(){{
					add("csv");
					add("xml");
					add("json");
				}};
				String conId;
				JsonObject respBody = new JsonObject();
				if(downloadDest.contains(destination)) {
					credentials.setCurrDestName(destination);
				}
				if((credentials.getCurrDestName()!=null)&&(!credentials.getCurrDestName().equalsIgnoreCase(destination))) {
					respBody.addProperty("message", "not matching dest name");
					respBody.addProperty("status", "401");
				}
				else if((credentials.getCurrDestName()==null)&&(!downloadDest.contains(destination))) {
					respBody.addProperty("message", "incorrect dest name");
					respBody.addProperty("status", "401");
				}
				else {
					conId = credentials.getUserId() + "_" + credentials.getCurrSrcName() + "_" + destination.toLowerCase() + "_"
							+ String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
					Gson gson = new Gson();
//					String schedule = filteredEndpoints.get("scheduled");
//					String period = filteredEndpoints.get("period");
//					String schedule = "true";
//					String period = "120";
					JsonArray temp2,endpoints = new JsonArray();
					JsonObject temp1;
					JsonElement temp = gson.fromJson(filteredEndpoints, JsonElement.class)
							.getAsJsonObject();
					for(Entry<String, JsonElement> e : temp.getAsJsonObject().entrySet()) {
						temp1  = new JsonObject();
						temp2 = new JsonArray();
						temp1.addProperty("name",e.getKey());
						temp1.addProperty("key",e.getKey());
						
						for(Entry<String, JsonElement> e1 : e.getValue().getAsJsonObject().entrySet()) {
							if(e1.getValue().getAsString().equalsIgnoreCase("true"))
								temp2.add(e1.getKey());
						}
						temp1.add("value", temp2);
						endpoints.add(temp1);
					}
					System.out.println(endpoints);
//					JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
//							.getAsJsonObject().get("endpoints").getAsJsonArray();
					ConnObj currobj = new ConnObj();
					Endpoint endpoint = new Endpoint();
					for (JsonElement obj : endpoints) {
						endpoint = gson.fromJson(obj, Endpoint.class);
						currobj.setEndPoints(endpoint);
					}
					currobj.setConnectionId(conId);
					currobj.setSourceName(credentials.getCurrSrcName());
					currobj.setDestName(destination.toLowerCase());
					currobj.setPeriod(Integer.parseInt(period)*1000);
					currobj.setScheduled(schedule);
					
					credentials.setCurrConnId(currobj);
					System.out.println("DatasourceController:createdatasource\t"+credentials.getCurrConnId().getConnectionId());
					credentials.setConnectionIds(conId, currobj);	
					
					JsonObject jsonObj;
					JsonArray endPointsArray = new JsonArray();
					
					for(Endpoint end: currobj.getEndPoints()) {
						JsonObject temp4 = new JsonObject();
						temp4.addProperty("key",end.getName());
						temp4.add("value", gson.fromJson(gson.toJson(end.getValue()),JsonArray.class));
						endPointsArray.add(temp4);
					}
					
					JsonArray eachArray = new JsonArray();
					JsonObject values = new JsonObject();
					values.addProperty("sourceName", credentials.getCurrSrcName().toLowerCase());
					values.addProperty("destName", destination.toLowerCase());
					values.addProperty("connectionId", conId.toLowerCase());
					values.addProperty("scheduled", schedule);
					values.addProperty("period", period);
					values.add("endPoints", endPointsArray);
					eachArray.add(values);				
					if (credentials.isUserExist()) {
						// userCredentials
						JsonObject eachObj = new JsonObject();
						eachObj.add("$each", eachArray);
						jsonObj = new JsonObject();
						jsonObj.add("srcdestId", eachObj);
						JsonObject addToSetObj = new JsonObject();
						addToSetObj.add("$addToSet", jsonObj);
						credentials.setUserExist(Utilities.postpatchMetaData(addToSetObj, "user", "PATCH",credentials.getUserId(),config.getMongoUrl()));
					} else {
						// userCredentials
						jsonObj = new JsonObject();
						jsonObj.add("srcdestId", eachArray);
						jsonObj.addProperty("_id", credentials.getUserId().toLowerCase());
						credentials.setUserExist(Utilities.postpatchMetaData(jsonObj, "user", "POST",credentials.getUserId(),config.getMongoUrl()));
					}
					applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));
					
					applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(), credentials.getDestObj(),credentials.getSrcToken() , credentials.getDestToken(),
							credentials.getCurrSrcName(), destination.toLowerCase(), credentials.getUserId()));				
					credentials.setCurrConnId(currobj);
					
					if(choice.equalsIgnoreCase("export")) {
						String out = Context.getBean(DataController.class).checkConnection("export", credentials.getCurrConnId().getConnectionId(), session).getBody();
						respBody.addProperty("data", out);
					}
					else if(choice.equalsIgnoreCase("download")) {
						JsonObject out = Context.getBean(DataController.class).fordownload(gson.fromJson(filteredEndpoints, JsonElement.class)
								.getAsJsonObject(), session).getBody();
						respBody.add("data", out);
					}
					
	    			respBody.addProperty("message", "DataSource created");
					respBody.addProperty("status", "200");
				}				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	
}