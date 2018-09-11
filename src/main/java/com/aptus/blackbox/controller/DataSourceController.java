package com.aptus.blackbox.controller;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.DestinationConfigService;
import com.aptus.blackbox.dataServices.MeteringService;
import com.aptus.blackbox.dataServices.SchedulingService;
import com.aptus.blackbox.dataServices.SourceConfigService;
import com.aptus.blackbox.dataServices.SrcDestCredentialsService;
import com.aptus.blackbox.dataServices.SrcDestListService;
import com.aptus.blackbox.dataServices.UserConnectorService;
import com.aptus.blackbox.dataServices.WebSocketService;
import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.datamodels.SrcDestCredentials;
import com.aptus.blackbox.datamodels.SrcDestList;
import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
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
	private SourceConfigService sourceConfigService;
	@Autowired
	private DestinationConfigService destinationConfigService;
	@Autowired
	private SrcDestCredentialsService srcDestCredentialsService;
	@Autowired
	private UserConnectorService userConnectorSerive;
	@Autowired
	private ApplicationContext Context;
	@Autowired
	private MeteringService meteringService;
	@Autowired
	private SrcDestListService srcDestListService;
	@Autowired
	private SchedulingService schedulingService;
	@Autowired
	private WebSocketService socketService;
	
	
	
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

	/*public void srcDestIdOLD(String type, String srcdestId) {
		//change return type to void
		try {
			srcdestId = srcdestId.toUpperCase();
			System.out.println(type+ " "+srcdestId);
			Parser parse = Context.getBean(Parser.class);
			if (type.equalsIgnoreCase("source")) {	
				    
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
	}*/
	
	public void srcDestId(String type, String srcdestId) {
		//change return type to void
		
			try {
				
				System.out.println(type+ " "+srcdestId);
				
				if (type.equalsIgnoreCase("source")) {	
					    SourceConfig srcobj=  sourceConfigService.getSourceConfig(srcdestId);
					    if(srcobj == null) {
					    	logger.error("Source Config Object is null");
					    }
						credentials.setSrcObj(srcobj);
						credentials.setCurrSrcName(srcdestId.toLowerCase());
						credentials.setCurrSrcValid(false);
						

				} else {			
						DestinationConfig destObj = destinationConfigService.getDestinationConfig(srcdestId);
						if(destObj == null) {
					    	logger.error("Destination Config Object is null");
					    }
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

	@RequestMapping(method = RequestMethod.GET, value = "/validate")
	private ResponseEntity<String> validate(@RequestParam("type") String type,@RequestParam("srcdestId") String srcdestId,HttpSession session,
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
			
			
			if(session.getId()==applicationCredentials.getSessionId(credentials.getUserId())) {
				
				credentials.setCurrConnObj(null);///check here
				
				srcDestId(type,srcdestId);
				System.out.println("inside validate function:"+credentials.getSrcObj());
				String credentialId = credentials.getUserId()+"_";
				/*
				 * 	Check and sets if user src/dest exist						
				 */
				if (type.equalsIgnoreCase("source")) {
					credentials.setCurrSrcId(credentials.getUserId().toLowerCase()+"_"+credentials.getCurrSrcName().toLowerCase());
					System.out.println("Auth Req: "+credentials.getSrcObj().getAuthtype());
					if( credentials.getSrcObj().getAuthtype().equalsIgnoreCase("NoAuth"))
						{
						
						System.out.println("No Authentication");
						String url="/close.html";
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						
						return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
						}
					
					credentialId += credentials.getCurrSrcName();
					boolean usrSrcExist = srcDestCredentialsService.srcDestCredentialsExist(credentialId.toLowerCase(), Constants.COLLECTION_SOURCECREDENTIALS);
					credentials.setUsrSrcExist(usrSrcExist);
					System.out.println("User Source Exist : "+credentials.isUsrSrcExist());
				}
				else {
					credentials.setCurrDestId(credentials.getUserId().toLowerCase() + "_" + credentials.getCurrDestName().toLowerCase()+"_"+database_name);
							
					credentialId += credentials.getCurrDestName();
					boolean usrDestExist = srcDestCredentialsService.srcDestCredentialsExist(credentialId.toLowerCase(), Constants.COLLECTION_DESTINATIONCREDENTIALS);
					credentials.setUsrDestExist(usrDestExist);
					System.out.println("User Destination Exist : "+credentials.isUsrDestExist());
				}
				return initialiser1(type,database_name,db_username,db_password,server_host,server_port);
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

	
	private ResponseEntity<String> initialiser1(String type,String database_name,String db_username,String db_password,String server_host,String server_port) {
		//add destination fetch and validation
		System.out.println("inside initialiser1 function");
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		

		headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");

		try {			
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {				
				if(type.equalsIgnoreCase("source")) {
					credentials.setCurrSrcValid(false);
					fetchSrcCred1();
					
					
					System.out.println(type+" credentials already exist"+credentials.getSrcObj().get_id());
					
					System.out.println(credentials.getSrcObj()+" "+credentials);
					out = Utilities.token(credentials.getSrcObj().getValidateCredentials(),credentials.getSrcToken(),credentials.getUserId()+"DataSourceController.initialiser1");
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
				logger.info("New credentials fetch");
				
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
			
			logger.debug("Inside initialiser catch");
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			//System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		
		catch (Exception e) {
			e.printStackTrace();
			logger.debug("home.init");
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
		
			SrcDestCredentials srcDestCredential = srcDestCredentialsService.
					getCredentials(credentialId, Constants.COLLECTION_SOURCECREDENTIALS);
			
			
			for(Map<String,String> hm:srcDestCredential.getCredentials()) {
				String k = null,v = null;
						for(Map.Entry<String, String> map : hm.entrySet()) {
							if(map.getKey().equals("key")) 
								k = map.getValue();
							else if(map.getKey().equals("value")) 
								v = map.getValue();
						}
				System.out.println("Key = "+k);
				System.out.println("Val = "+v+"\n");
				
				credentials.addSrcToken(k,v);
					
			}
			
			
		} 
		
		catch (HttpStatusCodeException e) {
			
			logger.debug("Inside fetchSrcCred catch");
			e.getStatusCode();
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			
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
//		headers.add("Cache-Control", "no-cache");
//		headers.add("access-control-allow-origin", config.getRootUrl());
//		headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				boolean isvalid = false;
				System.out.println(type+" "+srcDestId);
				if(type.equals("source")){
					
					
					if(credentials.getSrcObj().getAuthtype().equalsIgnoreCase("NoAuth")) {
						isvalid = true;
						credentials.setCurrSrcValid(isvalid);
						credentials.setSrcToken(new HashMap<String, String>());
						JsonObject jobject = new JsonObject();
						jobject.addProperty("isvalid",isvalid);
						//publish source credentials having no  authentication
						applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(), null, credentials.getSrcToken(), null, credentials.getCurrSrcName(), null,credentials.getCurrSrcId(),null, credentials.getUserId()));
						return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobject.toString());
					}
					
					if(!credentials.getCurrSrcName().equalsIgnoreCase(srcDestId)) {
						isvalid=false;
					}
					else {
						System.out.println("ISVALID");
						credentials.getSrcToken().entrySet().iterator().forEachRemaining(a->{
							System.out.println("key :: "+a.getKey());
							System.out.println("val :: "+a.getValue()+"\n");
						});
						ResponseEntity<String> out = Utilities.token(credentials.getSrcObj().getValidateCredentials(), credentials.getSrcToken(), "DataSourceController.isvalid");
						isvalid = out.getStatusCode().is2xxSuccessful();
						if(isvalid)
						{
							Context.getBean(SourceController.class).saveValues(out);
							//publish source credentials having authentication
							applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(), null, credentials.getSrcToken(), null, credentials.getCurrSrcName(), null,credentials.getCurrSrcId(),null, credentials.getUserId()));
						}
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
					if(isvalid) {
						applicationEventPublisher.publishEvent(new PushCredentials(null, credentials.getDestObj(), null, credentials.getDestToken(), null, credentials.getCurrDestName(),null,credentials.getCurrDestId(), credentials.getUserId()));
					}
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
               
            	boolean deleteUserConnector = userConnectorSerive.deleteConnectorObject(credentials.getUserId(), connId);
                System.out.println("Delete user Connector: "+deleteUserConnector);
                
            	if (deleteUserConnector) { 
                	
            		socketService.sendUserStatistics();
                	//applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));
                	
                	System.out.println("UserID:"+applicationCredentials.getApplicationCred().get(credentials.getUserId()));
                	System.out.println("ConnID:"+applicationCredentials.getApplicationCred().get(credentials
            				.getUserId()).getSchedulingObjects().get(connId));
                	
                	if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
                		if(applicationCredentials.getApplicationCred().get(credentials
                				.getUserId()).getSchedulingObjects().get(connId)!=null) {
                			System.out.println("--**PUBLISHING to INTERRUPT THREAD");
                			applicationEventPublisher.publishEvent(new InterruptThread(applicationCredentials.getApplicationCred().get(credentials
                    				.getUserId()).getSchedulingObjects().get(connId).getThread()
                					, false
                					, credentials.getUserId(), credentials.getCurrConnObj().getConnectionId()));
                			System.out.println("--**CAlling delete connection");
                			System.out.println("Delete user scheduleobj: "+schedulingService.deleteConnection(credentials.getUserId(), connId));
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
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/deletedatasourceOLD")
    private ResponseEntity<String> deleteDataSourceOLD(HttpSession session, @RequestParam("connId") String connId) {
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
                	socketService.sendUserStatistics();
                	//applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));
                	
                	if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
                		if(applicationCredentials.getApplicationCred().get(credentials
                				.getUserId()).getSchedulingObjects().get(connId)!=null) {
                			applicationEventPublisher.publishEvent(new InterruptThread(applicationCredentials.getApplicationCred().get(credentials
                    				.getUserId()).getSchedulingObjects().get(connId).getThread()
                					, false, credentials.getUserId(), credentials.getCurrConnObj().getConnectionId()));
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
					currobj.setSourceId(credentials.getCurrSrcId());
					currobj.setDestinationId(credentials.getCurrDestId());
					
					credentials.setCurrConnObj(currobj);
					
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
	
	/*@RequestMapping(method = RequestMethod.GET, value = "/createdatasourceOld")
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
					
					credentials.setCurrConnObj(currobj);
					System.out.println("DatasourceController:createdatasource\t"+credentials.getCurrConnObj().getConnectionId());
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
					credentials.setCurrConnObj(currobj);
					
					if(choice.equalsIgnoreCase("export")) {
						String out = Context.getBean(DataController.class).checkConnection("export", credentials.getCurrConnObj().getConnectionId(), session).getBody();
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
	*/
		
	@RequestMapping(method = RequestMethod.GET, value = "/createdatasource")
	private ResponseEntity<String> createDataSource1(@RequestParam(value = "filteredEndpoints", required=false) String filteredEndpoints,
			@RequestParam(value="scheduled") String schedule,
			@RequestParam(value="period",required=false) String period,
			@RequestParam(value="destination") String destination,
			@RequestParam(value="choice") String choice,
			HttpSession session) {
		ResponseEntity<String> ret = null;
		try {
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
//					String 
					period = "30";
					if(schedule.equalsIgnoreCase("false"))
						period="0";
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
					currobj.setSourceId(credentials.getCurrSrcId());
					currobj.setDestinationId(credentials.getCurrDestId());
					
					credentials.setCurrConnObj(currobj);
					System.out.println("DatasourceController:createdatasource\t"+credentials.getCurrConnObj().getConnectionId());
					credentials.setConnectionIds(conId, currobj);	
					
					System.out.println("Data Source Created");
					
					//add to userConnectors
					userConnectorSerive.addConnectorObj(credentials.getUserId(), currobj);
					System.out.println("Data Source connector pushed ");
					
					//insert
					meteringService.addConnection(credentials.getUserId(), conId, new ConnectionMetering());
					System.out.println("Metering Data pushed");
					
					
				/*	//publish credentials
					applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(), credentials.getDestObj(),credentials.getSrcToken() , credentials.getDestToken(),
							credentials.getCurrSrcName(), destination.toLowerCase(), credentials.getUserId()));				
					System.out.println("Data Source credentials pushed");
					*/
					credentials.setCurrConnObj(currobj);
					
					
					//applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));
					socketService.sendUserStatistics();
					
					if(choice.equalsIgnoreCase("export")) {
						String out = Context.getBean(DataController.class).checkConnection("export", credentials.getCurrConnObj().getConnectionId(), session).getBody();
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
    			respBody.addProperty("message", "Sorry! Your saptus"
    					+ "ession has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}