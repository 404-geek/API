package com.aptus.blackbox.controller;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.index.Parser;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.DestObject;
import com.aptus.blackbox.models.Endpoint;
import com.aptus.blackbox.models.SrcObject;
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
	
	private SrcObject srcObj;
	private DestObject destObj;
	/*
	 * input:  type(source/destination) and its name
	 * Parses its configuration file and stores it in credentials
	 * output:void
	 */
	private void srcDestId(String type, String srcdestId) {
		//change return type to void
		System.out.println(type+ " "+srcdestId);
		if (type.equalsIgnoreCase("source")) {
			
			srcObj = new Parser("source",srcdestId.toUpperCase(),config.getMongoUrl()).getSrcProp();
			credentials.setSrcObj(srcObj);
			credentials.setCurrSrcName(srcdestId.toLowerCase());
			credentials.setCurrSrcValid(false);
			
		} else {
			destObj = new Parser("destination",srcdestId.toUpperCase(),config.getMongoUrl()).getDestProp();
			credentials.setDestObj(destObj);
			credentials.setCurrDestName(srcdestId.toLowerCase());
			credentials.setCurrDestValid(false);
		}
		return;
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
		int res = 0;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				credentials.setCurrConnId(null);
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
					filter = "{\"_id\":{\"$regex\":\".*"+name.toLowerCase() + ".*\"}}";
				}						
				url = config.getMongoUrl()+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
				 
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
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.verifyuser");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	private ResponseEntity<String> initialiser(String type,String database_name,String db_username,String db_password,String server_host,String server_port) {
		//add destination fetch and validation
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {			
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {				
				if(type.equalsIgnoreCase("source")) {
					credentials.setCurrDestValid(false);
					fetchSrcCred();
					System.out.println(type+" credentials already exist");
					System.out.println(srcObj+" "+credentials);
					out = token(srcObj.getValidateCredentials(),credentials.getSrcToken(),credentials.getUserId()+"DataSourceController.initialiser");
					System.out.println("OUt:"+out);
					System.out.println("OUt:"+out.getStatusCode());
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
						System.out.println(url);	
						URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
						headers.setLocation(uri);
						out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
					}
				}
				else {
					credentials.setCurrDestValid(false);
					String url =  "/authdestination?"+
							"database_name="+database_name+
							"&db_username="+db_username+
							"&db_password="+db_password+
							"&server_host="+server_host+
							"&server_port="+server_port;
					System.out.println(url);	
					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
					headers.setLocation(uri);
					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);					
				}				
			} 
			else {
				String url;
				if (type.equalsIgnoreCase("source"))
					url = "/authsource";
				else
					url =  "/authdestination?"+
							"database_name="+database_name+
							"&db_username="+db_username+
							"&db_password="+db_password+
							"&server_host="+server_host+
							"&server_port="+server_port;
				System.out.println(url);
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				headers.setLocation(uri);
				out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("home.init");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	private void fetchSrcCred() {
		ResponseEntity<String> out = null;
		int res = 0;
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
				credentials.setSrcToken(key,value);
			}
			System.out.println(credentials.getSrcToken().keySet()+" : "+credentials.getSrcToken().values());
		} catch (Exception e) {
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
			if(Utilities.isSessionValid(session,credentials)) {
				boolean isvalid = false;
				System.out.println(type+" "+srcDestId);
				if(type.equals("source")){
					if(!credentials.getCurrSrcName().equalsIgnoreCase(srcDestId)) {
						credentials.setCurrSrcValid(false);
					}
					isvalid=credentials.isCurrSrcValid();
				}
				else if(type.equals("destination")) {
					if(!credentials.getCurrDestName().equalsIgnoreCase(srcDestId)) {
						credentials.setCurrDestValid(false);
					}
					isvalid=credentials.isCurrDestValid();
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
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
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
            if (Utilities.isSessionValid(session, credentials)) {
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/createdatasource")
	private ResponseEntity<String> createDataSource(@RequestParam(value = "filteredEndpoints", required=false) Map<String, String> filteredEndpoints,
			HttpSession session) {
		ResponseEntity<String> ret = null;
		try {
			filteredEndpoints = new HashMap<>();
			filteredEndpoints.put("filteredendpoints", "{\"endpoints\": [{\n" + 
					"		\"key\":\"Others\",\n" + 
					"		\"value\": [\n" + 
					"			\"Leads\",\n" + 
					"			\"Accounts\"\n" + 
					"		]}]}");
			System.out.println(filteredEndpoints.getClass());
			System.out.println(filteredEndpoints.get("filteredendpoints") + " " + filteredEndpoints.keySet());
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
			headers.add("access-control-allow-credentials", "true");
			if (Utilities.isSessionValid(session, credentials)) {
				// if(validateCredentials==null||endPoints==null||refreshToken==null) {
				// init();
				// }
				String conId;								
				conId = credentials.getUserId() + "_" + credentials.getCurrSrcName() + "_" + credentials.getCurrDestName() + "_"
						+ String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
				Gson gson = new Gson();
//				String schedule = filteredEndpoints.get("scheduled");
//				String period = filteredEndpoints.get("period");
				String schedule = "false";
				String period = "120";
				JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
						.getAsJsonObject().get("endpoints").getAsJsonArray();
				ConnObj currobj = new ConnObj();
				Endpoint endpoint = new Endpoint();
				for (JsonElement obj : endpoints) {
					endpoint = gson.fromJson(obj, Endpoint.class);
					currobj.setEndPoints(endpoint);
				}
				currobj.setConnectionId(conId);
				currobj.setSourceName(credentials.getCurrSrcName());
				currobj.setDestName(credentials.getCurrDestName());
				currobj.setPeriod((Integer.parseInt(period)*1000));
				currobj.setScheduled(schedule);
				credentials.setCurrConnId(currobj);
				credentials.setConnectionIds(conId, currobj);
				
				JsonObject jsonObj;
				JsonArray endPointsArray = new JsonArray();
				
				for(Endpoint end: currobj.getEndPoints()) {
					JsonObject temp = new JsonObject();
					temp.addProperty("key",end.getKey());
					temp.add("value", gson.fromJson(gson.toJson(end.getValue()),JsonArray.class));
					endPointsArray.add(temp);
				}
				
				JsonArray eachArray = new JsonArray();
				JsonObject values = new JsonObject();
				values.addProperty("sourceName", credentials.getCurrSrcName().toLowerCase());
				values.addProperty("destName", credentials.getCurrDestName().toLowerCase());
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
					JsonObject addToSetObj = new JsonObject();
					jsonObj.addProperty("_id", credentials.getUserId().toLowerCase());
					credentials.setUserExist(Utilities.postpatchMetaData(jsonObj, "user", "POST",credentials.getUserId(),config.getMongoUrl()));
				}
				applicationEventPublisher.publishEvent(new PushCredentials(srcObj, destObj,credentials.getSrcToken() , credentials.getDestToken(),
						credentials.getCurrSrcName(), credentials.getCurrDestName(), credentials.getUserId()));				
				JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "DataSource created");
				respBody.addProperty("status", "200");
				return new ResponseEntity<String>(respBody.toString(), headers, HttpStatus.OK);
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
