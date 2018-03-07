package com.aptus.blackbox.controller;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.ConnObj;
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.Parser;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
@RestController
public class DataSourceController {
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
	/*
	 * input:  type(source/destination) and its name
	 * Parses its configuration file and stores it in credentials
	 * output:void
	 */
	private void srcDestId(String type, String srcdestId) {
		//change return type to void
		System.out.println(type+ " "+srcdestId);
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
		headers.add("access-control-allow-origin", rootUrl);
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
					name = credentials.getSrcName();
					filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase()+"_"+name.toLowerCase() + "\"}";
				}					
				else {
					name = credentials.getDestName();
					filter = "{\"_id\":{\"$regex\":\".*"+name.toLowerCase() + ".*\"}}";
				}						
				url = mongoUrl+"/credentials/" + type.toLowerCase() + "Credentials?filter=" + filter;
				 
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();				
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
				out = initialiser(type,database_name,db_username,db_password,server_host,server_port);
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
		try {
			HttpHeaders headers = new HttpHeaders();
			headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if (credentials.isUsrSrcExist() || credentials.isUsrDestExist()) {				
				if(type.equalsIgnoreCase("source")) {					
					fetchSrcCred(type);
					System.out.println(srcObj+" "+credentials);
					out = Utilities.token(srcObj.getValidateCredentials(),credentials.getSrcToken());
					if (out.getStatusCode().is2xxSuccessful()) {
						System.out.println(type + "tick");
						Utilities.valid();
						credentials.setSrcValid(true);
						String url=homeUrl;
						headers.setLocation(URI.create(url+"/close.html"));
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
					credentials.setDestValid(false);
					String url =  baseUrl+"/authdestination?"+
							"database_name="+database_name+
							"&db_username="+db_username+
							"&db_password="+db_password+
							"&server_host="+server_host+
							"&server_port="+server_port;
					System.out.println(url);					
					headers.setLocation(URI.create(url));
					out = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);					
				}				
			} 
			else {
				String url;
				if (type.equalsIgnoreCase("source"))
					url = baseUrl+"/authsource";
				else
					url =  baseUrl+"/authdestination?"+
							"database_name="+database_name+
							"&db_username="+db_username+
							"&db_password="+db_password+
							"&server_host="+server_host+
							"&server_port="+server_port;
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

	private void fetchSrcCred(String type) {
		ResponseEntity<String> out = null;
		int res = 0;
		String userid = credentials.getUserId(), appId;
		try {
			appId = credentials.getSrcName();
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/isvalid")
	private ResponseEntity<String> isValid(@RequestParam("type") String type,
			@RequestParam("srcdestId") String srcDestId, HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				boolean isvalid = false;
				System.out.println(type+" "+srcDestId);
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
        headers.add("access-control-allow-origin", rootUrl);
        headers.add("access-control-allow-credentials", "true");
        try {         
            HttpEntity<?> httpEntity;
            if (Utilities.isSessionValid(session, credentials)) {
                String url = mongoUrl + "/credentials/userCredentials/" + credentials.getUserId();
                System.out.println("DeleteDataSource");
                System.out.println(url);
                JsonObject obj1 = new JsonObject();
                obj1.addProperty("connectionId", connId);
                JsonObject obj2 = new JsonObject();
                obj2.add("srcdestId", obj1);
                JsonObject body = new JsonObject();
                body.add("$pull", obj2);
                URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
                headers.add("Content-Type", "application/json");
                System.out.println("68542168521"+body.toString());
                httpEntity = new HttpEntity<Object>(body.toString(), headers);
                RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
                out = restTemplate.exchange(URI.create(url), HttpMethod.PATCH, httpEntity, String.class);
                if (out.getStatusCode().is2xxSuccessful()) {                   
                    System.out.println(connId + "***********Deleted!!!!**************");
                    JsonObject respBody = new JsonObject();
                    respBody.addProperty("data", "Same");
                    respBody.addProperty("status", "14");
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/createdatasource")
	private ResponseEntity<String> createDataSource(@RequestParam Map<String, String> filteredEndpoints,
			HttpSession session) {
		ResponseEntity<String> ret = null;
		try {
			System.out.println(filteredEndpoints.getClass());
			System.out.println(filteredEndpoints.get("filteredendpoints") + " " + filteredEndpoints.keySet());
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if (Utilities.isSessionValid(session, credentials)) {
				// if(validateCredentials==null||endPoints==null||refreshToken==null) {
				// init();
				// }
				JsonArray sourceBody,destBody;
				String endpnts = "", conId;
				
				sourceBody = new JsonArray();
				destBody =  new JsonArray();
				
				conId = credentials.getUserId() + "_" + credentials.getSrcName() + "_" + credentials.getDestName() + "_"
						+ String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
				for (Map.Entry<String, String> mp : credentials.getSrcToken().entrySet()) {
					JsonObject tmp = new JsonObject();
					tmp.addProperty("key", String.valueOf(mp.getKey()));
					tmp.addProperty("value", String.valueOf(mp.getValue()));
					sourceBody.add(tmp);
				}
				for (Map.Entry<String, String> mp : credentials.getDestToken().entrySet()) {
					JsonObject tmp = new JsonObject();
					tmp.addProperty("key", String.valueOf(mp.getKey()));
					tmp.addProperty("value", String.valueOf(mp.getValue()));
					destBody.add(tmp);
				}
				Gson gson = new Gson();
				JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
						.getAsJsonObject().get("endpoints").getAsJsonArray();
				ConnObj currobj = new ConnObj();
				for (JsonElement obj : endpoints) {
					endpnts += obj.getAsString() + ",";
					currobj.setEndPoints(obj.getAsString());
				}
				currobj.setConnectionId(conId);
				currobj.setSourceName(credentials.getSrcName());
				currobj.setDestName(credentials.getDestName());
				credentials.setCurrConnId(currobj);
				endpnts = endpnts.substring(0, endpnts.length() - 1).toLowerCase();
				System.out.println(sourceBody);
				System.out.println(destBody);
				JsonObject jsonObj;
				JsonArray endPointsArray = new JsonArray();
				endPointsArray.add(endpnts);
				JsonArray eachArray = new JsonArray();
				JsonObject values = new JsonObject();
				values.addProperty("sourceName", credentials.getSrcName().toLowerCase());
				values.addProperty("destName", credentials.getDestName().toLowerCase());
				values.addProperty("connectionId", conId.toLowerCase());
				values.add("endPoints", endPointsArray);
				eachArray.add(values);
				JsonObject eachObj = new JsonObject();
				eachObj.add("$each", eachArray);
				jsonObj = new JsonObject();
				jsonObj.add("srcdestId", eachObj);
				if (credentials.isUserExist()) {
					// userCredentials
					JsonObject addToSetObj = new JsonObject();
					addToSetObj.add("$addToSet", jsonObj);
					postpatchMetaData(addToSetObj, "user", "PATCH");
				} else {
					// userCredentials
					jsonObj.addProperty("_id", credentials.getUserId().toLowerCase());
					postpatchMetaData(jsonObj, "user", "POST");
				}
				// sourceCredentials
				jsonObj = new JsonObject();
				jsonObj.addProperty("_id",
						credentials.getUserId().toLowerCase() + "_" + credentials.getSrcName().toLowerCase());
				jsonObj.add("credentials", sourceBody);
				postpatchMetaData(jsonObj, "source", "POST");
				// destCredentials
				jsonObj = new JsonObject();				
				jsonObj.addProperty("_id",
						credentials.getUserId().toLowerCase() + "_" + credentials.getDestName().toLowerCase() + "_"
								+ credentials.getDestToken().get("database_name"));
				jsonObj.add("credentials", destBody);
				postpatchMetaData(jsonObj, "destination", "POST");
				// ret = data(credentials.getSrcName());
				// System.out.println(ret.getBody());
				String url = baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("", headers, HttpStatus.OK);
			} else {
				System.out.println("Session expired!");
				String url = baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired", headers, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	private void postpatchMetaData(JsonObject body, String type, String method) {
		try {

			System.out.println("postpatchMetaData:\nBody: "+body.toString()+"\nType: "+type+"\nMethod"+method);
			ResponseEntity<String> out = null;
			String url = "";
			String appname = "";
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			HttpMethod met = null;
			String filter = "";
			if (method.equalsIgnoreCase("POST")) {
				url = mongoUrl + "/credentials/" + type.toLowerCase() + "Credentials";
				met = HttpMethod.POST;
			} else if (method.equalsIgnoreCase("PATCH")) {
				url = mongoUrl + "/credentials/" + type.toLowerCase() + "Credentials/"
						+ credentials.getUserId().toLowerCase();
				met = HttpMethod.PATCH;
			} 
			System.out.println(url + "\n" + met);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(), headers);
			out = restTemplate.exchange(url, met, httpEntity, String.class, filter);
			if (out.getStatusCode().is2xxSuccessful()) {
				credentials.setUserExist(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.postpatchmetadata");
		}
		return;
	}

//	@RequestMapping(method = RequestMethod.POST, value = "/createdatasource")
//	private ResponseEntity<String> createDataSource(@RequestParam Map<String,String> filteredEndpoints,HttpSession session)
//	{		
//		ResponseEntity<String> ret = null;
//		//System.out.println(srcname+" "+destname);
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Cache-Control", "no-cache");
//		headers.add("access-control-allow-origin", rootUrl);
//		headers.add("access-control-allow-credentials", "true");
//		try	{
//			System.out.println(filteredEndpoints.getClass());
//			System.out.println(filteredEndpoints.get("filteredendpoints")+" "+filteredEndpoints.keySet());
//			if(Utilities.isSessionValid(session,credentials)) {
////				if(validateCredentials==null||endPoints==null||refreshToken==null) {
////					init();
////				}			
//				String body="",sourceBody="",destBody="",endpnts="",conId;
//				conId=credentials.getUserId()+"_"+credentials.getSrcName()+"_"+credentials.getDestName()
//						+"_"+String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
//				for(Map.Entry<String,String> mp:credentials.getSrcToken().entrySet()) {
//					sourceBody+="{\"key\":\""+String.valueOf(mp.getKey())+"\","
//							+ "\"value\":\""+String.valueOf(mp.getValue())+"\"},";
//				}
//				for(Map.Entry<String,String> mp:credentials.getDestToken().entrySet()) {
//					destBody+="{\"key\":\""+String.valueOf(mp.getKey())+"\","
//							+ "\"value\":\""+String.valueOf(mp.getValue())+"\"},";
//				}
//				Gson gson =new Gson();
//				JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
//						.getAsJsonObject().get("endpoints").getAsJsonArray();
//				ConnObj currobj = new ConnObj();
//				for(JsonElement obj:endpoints) {
//					endpnts+="\""+obj.getAsString()+"\",";
//					currobj.setEndPoints(obj.getAsString());
//				}
//				currobj.setConnectionId(conId);
//				currobj.setSourceName(credentials.getSrcName());
//				currobj.setDestName(credentials.getDestName());
//				credentials.setCurrConnId(currobj);
//				endpnts = endpnts.substring(0, endpnts.length()-1).toLowerCase();
//				System.out.println(sourceBody);
//				System.out.println(destBody);
//				if((!sourceBody.isEmpty())&&(!destBody.isEmpty())) {
//					sourceBody=sourceBody.substring(0,sourceBody.length()-1);
//					destBody=destBody.substring(0,destBody.length()-1);
//				}
//				if(credentials.isUserExist())
//				{
//					//sourceCredentials
//					body= "{\"credentials\":["+sourceBody+"]}";
//					
//					postpatchMetaData(body,"source","PATCHapp");
//					
//					//userCredentials
//					body = "{\"$addToSet\":"
//							+ "{\"srcdestId\":"
//							+ "{\"$each\":["
//							+ "{\"sourceName\": \""+credentials.getSrcName().toLowerCase()+"\","
//							+ "\"destName\":\""+credentials.getDestName().toLowerCase()+"\","
//							+ "\"connectionId\": \""+conId.toLowerCase()+"\","
//							+ "\"endPoints\":["+endpnts+"]}"
//							+ "]}}}";
//					
//					postpatchMetaData(body,"user","PATCH");
//					
//					//destCredentials
//					body= "{\"credentials\":["+destBody+"]}";
//					
//					postpatchMetaData(body,"destination","PATCHapp");
//					
//				}
//				else {
//					//userCredentials
//					body = "{ \"_id\" : \""+credentials.getUserId().toLowerCase()+"\","
//							+ "\"srcdestId\" : [ "
//							+ "{ \"sourceName\" : \""+credentials.getSrcName().toLowerCase()+"\" , "
//							+ "\"destName\" : \""+credentials.getDestName().toLowerCase()+"\","
//							+ "\"connectionId\":\""+conId.toLowerCase()+"\","
//							+ "\"endPoints\":["+endpnts+"]}"
//							+ "]}";
//					
//					postpatchMetaData(body,"user","POST");
//					
//					//sourceCredentials
//					body= "{\n" + 
//							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
//							+"_"+credentials.getSrcName().toLowerCase()+"\",\n" + 
//							"	\"credentials\":["+sourceBody+"]\n" +
//							"}";
//					 
//					postpatchMetaData(body,"source","POST");
//					
//					//destCredentials
//					body= "{\n" + 
//							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
//							+"_"+credentials.getDestName().toLowerCase()
//							+"_"+credentials.getDestToken().get("database_name")+"\",\n" +
//							"				\"credentials\":["+destBody+"]\n" + 
//							"}";
//					
//					postpatchMetaData(body,"destination","POST");
//				}
//				//ret = data(credentials.getSrcName());
//				//System.out.println(ret.getBody());
//				String url=baseUrl;
//				headers.setLocation(URI.create(url));
//				return new ResponseEntity<String>("",headers ,HttpStatus.OK);
//			}
//			else {
//   				System.out.println("Session expired!");
//    			JsonObject respBody = new JsonObject();
//    			respBody.addProperty("message", "Sorry! Your session has expired");
//				respBody.addProperty("status", "33");
//				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
//	}
//	private void postpatchMetaData(String body,String type,String method)
//	   {
//	       try {
//	           ResponseEntity<String> out = null;
//	           String url="";
//	           String appname = "";
//	           RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
//	           HttpMethod met=null;
//	           System.out.println(body);
//	           String filter="";
//	           if(method.equalsIgnoreCase("POST")) {
//	               url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials";
//	               met = HttpMethod.POST;
//	           }                
//	           else if(method.equalsIgnoreCase("PATCH")) {
//	               url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase();
//	               met = HttpMethod.PATCH;
//	           }
//	           else if(method.equalsIgnoreCase("PATCHapp")) {
//	        	   url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase()+"_"+appname.toLowerCase();
//	        	   if(type.equalsIgnoreCase("source")) {
//	        		   appname = credentials.getSrcName();	        		   
//	        	   }
//	        	   else {
//	        		   appname = credentials.getDestName();
//	        		   url +="_"+credentials.getDestToken().get("database_name");
//	        	   }	        	   
//	        	   met = HttpMethod.PATCH;
//	           }
//	           System.out.println(url+"\n"+met);
//	           HttpHeaders headers =new HttpHeaders();
//	           headers.add("Content-Type","application/json") ;
//	           headers.add("Cache-Control", "no-cache");
//	           headers.add("access-control-allow-origin", rootUrl);
//	            headers.add("access-control-allow-credentials", "true");
//	           HttpEntity<?> httpEntity = new HttpEntity<Object>(body,headers);
//	           out = restTemplate.exchange(url, met, httpEntity, String.class,filter);
//	           if (out.getStatusCode().is2xxSuccessful()) {
//	               credentials.setUserExist(true);
//	            }
//	       }
//	       catch(Exception e) {
//	           e.printStackTrace();
//	           System.out.println("source.postpatchmetadata");
//	       }
//	       return;    
//	   }
	
	@RequestMapping(value="/getconnectionids")
	private ResponseEntity<String> getConnectionIds(HttpSession session) {
		String dataSource=null;
		HttpHeaders headers = new HttpHeaders();			
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
        headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				ResponseEntity<String> out = null;
				RestTemplate restTemplate = new RestTemplate();
				//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				String url = mongoUrl+"/credentials/userCredentials/"+credentials.getUserId();
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
				
			    url = mongoUrl+"/credentials/SrcDstlist/srcdestlist";
			    uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				out  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				respBody.add("images",gson.fromJson(out.getBody(), JsonElement.class));
				
				
				JsonArray srcdestId = data.getAsJsonObject().get("srcdestId").getAsJsonArray();
				for(JsonElement ele:srcdestId) {
					conObj = gson.fromJson(ele, ConnObj.class);
					credentials.setConnectionIds(conObj.getConnectionId(), conObj);
				}
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
