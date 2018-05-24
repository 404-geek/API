package com.aptus.blackbox.controller;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.event.Metering;
import com.aptus.blackbox.event.PostExecutorComplete;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.event.ScheduleEventData;
import com.aptus.blackbox.DestinationAuthorisation;
import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.index.ScheduleInfo;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.Cursor;
import com.aptus.blackbox.models.DestObject;
import com.aptus.blackbox.models.Endpoint;
import com.aptus.blackbox.models.SrcObject;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;
import com.aptus.blackbox.utils.Utilities;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;


@RestController
public class DataController extends RESTFetch {
	private Connection con = null;

	@Autowired
	private Config config;
	@Autowired
	private Credentials credentials;
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationContext Context;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	
	/*
	 * 
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	private ResponseEntity<String> destination(HttpSession session,
			@RequestParam(value = "database_name") String database_name,
			@RequestParam(value = "db_username") String db_username,
			@RequestParam(value = "db_password") String db_password,
			@RequestParam(value = "server_host") String server_host,
			@RequestParam(value = "server_port") String server_port) throws SQLException { // @RequestParam("data")
																							// Map<String,String> data
		credentials.setCurrDestValid(false);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId())
						.setLastAccessTime(session.getLastAccessedTime());
				HashMap<String, String> destCred = new HashMap<>();
				destCred.put("database_name", database_name);
				destCred.put("db_username", db_username);
				destCred.put("db_password", db_password);
				destCred.put("server_host", server_host);
				destCred.put("server_port", server_port);
				// tableName = "user";
				credentials.setDestToken(destCred);
				DestObject destObj = credentials.getDestObj();
				Map<String, String> destToken = credentials.getDestToken();
				
				System.out.println(checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("message").toString());
				
				if (!checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("status").getAsBoolean())
				{
					credentials.setCurrDestValid(false);
					System.out.println("Invalid database credentials");
					URI uri = UriComponentsBuilder.fromUriString("/close.html").build().encode().toUri();
					headers.setLocation(uri);
					return new ResponseEntity<String>("", headers, HttpStatus.NOT_FOUND);
					
					// invalid
				}
				
				else if(checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("status").getAsBoolean())
				{
				credentials.setCurrDestValid(true);
				System.out.println("Database credentials validated");
				credentials.setDestToken(destCred);
				JsonObject jobject = new JsonObject();
				jobject.addProperty("isvalid",credentials.isCurrDestValid());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobject.toString());
				//String url = config.getHomeUrl();
//				URI uri = UriComponentsBuilder.fromUriString("/close.html").build().encode().toUri();
//				headers.setLocation(uri);
//				return new ResponseEntity<String>("", headers, HttpStatus.MOVED_PERMANENTLY);				
				}
			} else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con != null)
				con.close();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
public boolean pushDB(String jsonString, String tableName,DestObject destObj,Map<String, String> destToken) throws SQLException {
		System.out.println("pushDBController-driver: " + destObj.getDrivers());
		try {
			System.out.println("TABLENAME: " + tableName);
			// System.out.println("JSONSTRING: "+jsonString);

			PreparedStatement preparedStmt;
			System.out.println(checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("message").toString());
			if (checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("status").getAsBoolean()) {
				if (con == null || con.isClosed())
					connection(destToken, destObj);
				credentials.setCurrDestValid(true);
				JFlat x = new JFlat(jsonString);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				// System.out.println(json2csv);
				
				String statement = "";

				int i = 0;
				for (Object[] row : json2csv) {
					if (i == 0) {
						statement = "CREATE TABLE " + destObj.getIdentifier_quote_open() + tableName
								+ destObj.getIdentifier_quote_close() + "(";
						for (Object t : row)
							statement += t.toString().replace("_", "") +" "+credentials.getDestObj().getType_text()+ ",";

						statement = statement.substring(0, statement.length() - 1) + ");";
						System.out.println("-----" + statement);
						preparedStmt = con.prepareStatement(statement);
						preparedStmt.execute();
					} else {
						int k;
						String instmt = "INSERT INTO " + destObj.getIdentifier_quote_open() + tableName
								+ destObj.getIdentifier_quote_close() + " VALUES(";

						for (k = 0; k < row.length; k++)
							instmt += "?,";

						instmt = instmt.substring(0, instmt.length() - 1) + ");";
						PreparedStatement stmt = con.prepareStatement(instmt);

						k = 1;
						for (Object attr : row) {
							stmt.setString(k++, attr == null ? null : attr.toString());
						}
						// System.out.println(instmt);
						stmt.execute();
					}
					i++;
				}
				System.out.println("All pushed");
				con.close();
				return true;
			}
			credentials.setCurrDestValid(false);
		} catch (Exception e) {
			System.err.println("Got an exception!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public ResponseEntity<String> connection(Map<String, String> destToken, DestObject destObj) throws SQLException {
		try {

			System.out.println("DataController-driver: " + destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + destToken.get("server_host") + ":" + destToken.get("server_port")
					+ destObj.getDbnameseparator() + destToken.get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, destToken.get("db_username"), destToken.get("db_password"));
		} 
		
		
		catch (SQLException  e) {
			
			System.out.println("inside connection sql exception");
			
			JsonObject respBody = new JsonObject();
            respBody.addProperty("code", "0");
            respBody.addProperty("message", "connction error in client database");
            System.out.println(e.getMessage());
            System.out.println(respBody.toString());
            //return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			
			
			
			return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			// TODO Auto-generated catch block
			
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).headers(null).body(null);
	}


	public JsonObject checkDB(String dbase, Map<String, String> destToken, DestObject destObj) throws SQLException {
		JsonObject resbody = new JsonObject();
		try {
			
			if (con == null || con.isClosed())
				connection(destToken, destObj);
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema ="
					+ destObj.getValue_quote_open() + dbase + destObj.getValue_quote_close() + ";";
			System.out.println(query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next()) {
				con.close();
				
				resbody.addProperty("status", true);
				resbody.addProperty("message", "conncetion success");
				resbody.addProperty("code", "200");
				return resbody;
			}
			con.close();

		} catch (SQLException e) {
			
			System.out.println(e.getErrorCode());
			System.out.println("check clientdatabase");
			resbody.addProperty("status", false);
			resbody.addProperty("message", "conncetion failed to client database");
			resbody.addProperty("code", "0");
			return resbody;
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (Exception e) {
			
			resbody.addProperty("status", false);
			resbody.addProperty("message", "conncetion failed");
			resbody.addProperty("code", "500");
			return resbody;
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return resbody;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/getInfoEndpoints")
	private void getInfoEndpoints(){
		ResponseEntity<String> ret = null;
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
	
			try {
				RestTemplate restTemplate = new RestTemplate();
				Gson gson = new Gson();
				List<UrlObject> infoEndpoints = credentials.getSrcObj().getInfoEndpoints();
				HttpMethod method;
				for(UrlObject infoEndpoint:infoEndpoints) {
					
					String url = buildUrl(infoEndpoint, credentials.getSrcToken(),credentials.getUserId()+"getInfoEndpoints");
					System.out.println(url);
					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
					
					HttpHeaders headers = buildHeader(infoEndpoint, credentials.getSrcToken(),credentials.getUserId()+"getInfoEndpoints");
					HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
					
					if (!infoEndpoint.getResponseBody().isEmpty()) {
						MultiValueMap<String, String> preBody = buildBody(infoEndpoint, credentials.getSrcToken(),"getInfoEndpoints");
						Object postBody=null;
						for(objects head:infoEndpoint.getHeader())
						{
							if(head.getKey().equalsIgnoreCase("content-type")) {
								postBody=bodyBuilder(head.getValue(),preBody,"getInfoEndpoints");
								break;
							}
						}
						httpEntity = new HttpEntity<Object>(postBody, headers);
					} else {
						httpEntity = new HttpEntity<Object>(headers);
					}
					
					method = (infoEndpoint.getMethod() == "GET") ? HttpMethod.GET : HttpMethod.POST;
					ret = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
					List<String> list = new ArrayList<String>();
					JsonElement element = new Gson().fromJson(ret.getBody(), JsonElement.class);
					String arr[] = infoEndpoint.getData().split("::");
					list = Utilities.checkByPath(arr, 0, element, list);
					System.out.println(infoEndpoint.getLabel()+" : "+list);
					
				}
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/selectaction")
	private ResponseEntity<String> selectAction(@RequestParam("choice") String choice,
			@RequestParam("connId") String connId, HttpSession httpsession) {
		ResponseEntity<String> ret = null;
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(httpsession, credentials)) {
				System.out.println(credentials.getCurrConnId() + " "+ credentials.getDestObj()+" "+credentials.getSrcObj());
				if (credentials.getCurrConnId().getScheduled().equalsIgnoreCase("true")
						&& choice.equalsIgnoreCase("export")) {
					SchedulingObjects schObj = new SchedulingObjects();
					schObj.setDestObj(credentials.getDestObj());
					schObj.setDestToken(credentials.getDestToken());
					schObj.setSrcObj(credentials.getSrcObj());
					schObj.setSrcToken(credentials.getSrcToken());
					schObj.setPeriod(credentials.getCurrConnId().getPeriod());
					schObj.setNextPush(ZonedDateTime.now().toInstant().toEpochMilli());
					schObj.setLastPushed(ZonedDateTime.now().toInstant().toEpochMilli());
					schObj.setDestName(credentials.getCurrDestName());
					schObj.setSrcName(credentials.getCurrSrcName());
					for (Endpoint endpoint : credentials.getCurrConnId().getEndPoints()) {
						Map<String,Status> sat = new HashMap<>();
						for(String end :endpoint.getValue()) {
							sat.put(end, null);
						}
						schObj.setEndPointStatus(endpoint.getName(), sat);
					}
					if (applicationCredentials.getApplicationCred().keySet().contains(credentials.getUserId())) {
						applicationCredentials.getApplicationCred().get(credentials.getUserId())
								.setSchedulingObjects(schObj, connId);
					} else {
						ScheduleInfo scInfo = new ScheduleInfo();
						scInfo.setSchedulingObjects(schObj, connId);
						applicationCredentials.setApplicationCred(credentials.getUserId(), scInfo);
					}
					System.out.println("Publishing custom event. ");
					ScheduleEventData scheduleEventData = Context.getBean(ScheduleEventData.class);
					scheduleEventData.setData(credentials.getUserId(), connId, credentials.getCurrConnId().getPeriod());
					// Context.getAutowireCapableBeanFactory().autowireBean(scheduleEventData);
					//PostExecutorComplete post = new PostExecutorComplete(credentials.getUserId(), credentials.getCurrConnId().getConnectionId());
					applicationEventPublisher.publishEvent(scheduleEventData);

					JsonObject respBody = new JsonObject();
					respBody.addProperty("status", "21");
					respBody.addProperty("data", "published");
					return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
				}
				else {
					return validateSourceCred(choice);
					
				}
			} else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e + "home.data");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
	}
	private ResponseEntity<String> validateSourceCred(String choice) {
		ResponseEntity<String> ret = null;
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		try {
			SrcObject obj = credentials.getSrcObj();
			if (obj.getRefresh().equals("YES")) {
				ret = token(credentials.getSrcObj().getRefreshToken(), credentials.getSrcToken(),
						"DataController.validateSourceCred");
				if (!ret.getStatusCode().is2xxSuccessful()) {
					JsonObject respBody = new JsonObject();
					respBody.addProperty("message", "Re-authorize");
					respBody.addProperty("status", "51");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header)
							.body(respBody.toString());

				} else {
					// next piece of code is for saveValues
					try {
						credentials.getSrcToken().putAll(new Gson().fromJson(ret.getBody(), HashMap.class));
					} catch (Exception e) {
						for (String s : ret.getBody().toString().split("&")) {
							System.out.println(s);
							credentials.getSrcToken().put(s.split("=")[0], s.split("=")[1]);
						}
					}
					applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(),
							credentials.getDestObj(), credentials.getSrcToken(), credentials.getDestToken(),
							credentials.getCurrSrcName(), credentials.getCurrDestName(),
							credentials.getUserId()));
					System.out.println("token : " + credentials.getSrcToken().keySet() + ":"
							+ credentials.getSrcToken().values());
					ret = validateData(obj.getValidateCredentials(), obj.getDataEndPoints(), choice);
					return ret;
				}
			} else {
				ret = token(obj.getValidateCredentials(), credentials.getSrcToken(),
						"DataController.validateSourceCred");
				if (!ret.getStatusCode().is2xxSuccessful()) {
					credentials.setCurrSrcValid(false);
					JsonObject respBody = new JsonObject();
					respBody.addProperty("message", "Re-authorize");
					respBody.addProperty("status", "51");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header)
							.body(respBody.toString());

				} else {
					credentials.setCurrSrcValid(true);
					// ret = Utilities.token(endPoints.get(0),credentials.getSrcToken());
					ResponseEntity<String> out=null;
					if(choice.equalsIgnoreCase("export")||choice.equalsIgnoreCase("view"))
					{
						out = fetchEndpointsData(obj.getDataEndPoints(), choice);
					}
					else {
						JsonObject respBody = new JsonObject();
						respBody.addProperty("message", "Validated");
						respBody.addProperty("status", "200");
						return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
					}
					System.out.println(out.getBody().substring(0, 20));
					System.out.println("Headers Inside validateSourceCred "+out.getHeaders());
					return out;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
	}
	private ResponseEntity<String> validateData(UrlObject valid, List<UrlObject> endPoints, String choice) {
		ResponseEntity<String> ret = null;
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		try {
			ret = token(valid, credentials.getSrcToken(), "DataController.validateData");
			if (!ret.getStatusCode().is2xxSuccessful()) {
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Contact Support");
				respBody.addProperty("status", "52");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(respBody.toString());
			} else {
				if(choice.equalsIgnoreCase("export")||choice.equalsIgnoreCase("view"))
				{
					ret = fetchEndpointsData(endPoints, choice);
					return ret;
				}
				else {
					JsonObject respBody = new JsonObject();
					respBody.addProperty("message", "Validated");
					respBody.addProperty("status", "200");
					return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.validatedata");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
	}

	
	private ResponseEntity<String> fetchEndpointsData(List<UrlObject> endpoints, String choice) {
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		ResponseEntity<String> out = null;
		try {
			Gson gson = new Gson();
			JsonObject endPoint = new JsonObject();
			JsonObject data = new JsonObject();
			boolean success=true;
			int totalRows=0;
			Metering metring = new Metering();
			metring.setConnId(credentials.getCurrConnId().getConnectionId());
			metring.setTime(new Date()+"");
			metring.setType(choice);
			metring.setUserId(credentials.getUserId());
			
			Map<String,List<UrlObject>> endp = new HashMap<>();
			for(UrlObject object:endpoints) {
				if(endp.containsKey(object.getCatagory())) {
					endp.get(object.getCatagory()).add(object);
				}
				else {
					List<UrlObject> lst = new ArrayList<>();
					lst.add(object);
    				endp.put(object.getCatagory(), lst);
				}
			}
			
			for(Endpoint endpnt : credentials.getCurrConnId().getEndPoints()) {
				if(endpnt.getName().equalsIgnoreCase("others")) {
					for(UrlObject object:endp.get(endpnt.getName())) {
						
						System.out.println("LABEL1" + object.getLabel());
						boolean value = endpnt.getValue().contains(object.getLabel());
						System.out.println(value + " " + object.getLabel().toLowerCase());
						
						if(endpnt.getValue().contains(object.getLabel())){
								
								Map<JsonElement,Integer> data1 = paginate(choice,object);
								
								Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
								JsonElement datum=entry.getKey();
								Integer rows=entry.getValue();
								
								if(choice.equalsIgnoreCase("view")) {
									if(!datum.getAsJsonObject().get("status").toString().equalsIgnoreCase("21")) {
										success=false;
									}
								}
								else {
									totalRows+=rows;
									metring.setRowsFetched(object.getLabel().toLowerCase(), rows);
								}
								endPoint.add(object.getLabel(),datum);
							}
						
					}
				}
				else {
					UrlObject object = endp.get(endpnt.getName()).get(0);
					for(String endpntLable:endpnt.getValue()) {
						object.setLabel(endpntLable);
						Map<String,String> ne = new HashMap<>();
						ne.put(endpnt.getName(), endpntLable);
						object.setUrl(url(object.getUrl(), ne));
						System.out.println("LABEL1" + object.getLabel());
						boolean value = endpnt.getValue().contains(object.getLabel());
						System.out.println(value + " " + object.getLabel().toLowerCase());
						
						if(endpnt.getValue().contains(object.getLabel())){
								
								Map<JsonElement,Integer> data1 = paginate(choice,object);
								
								Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
								JsonElement datum=entry.getKey();
								Integer rows=entry.getValue();
								
								if(choice.equalsIgnoreCase("view")) {
									if(!datum.getAsJsonObject().get("status").toString().equalsIgnoreCase("21")) {
										success=false;
									}
								}
								else {
									totalRows+=rows;
									metring.setRowsFetched(object.getLabel().toLowerCase(), rows);
								}
								endPoint.add(object.getLabel(),datum);
							}
						}
					}
				}
			
			metring.setTotalRowsFetched(totalRows);
			if(!choice.equalsIgnoreCase("view")) {
				applicationEventPublisher.publishEvent(metring);
			}
			
			data.add("data", endPoint);
			data.addProperty("status", "21");
			data.addProperty("message", "succesful");
			if(success==true) {
				data.addProperty("status", "23");
				data.addProperty("message", "unsuccesful");
			}
			return 	ResponseEntity.status(HttpStatus.OK).headers(header).body(data.toString());	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e + "token");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
	}

	
	@RequestMapping("/downloadData")
	public ResponseEntity<byte[]> downloadData(@RequestParam("choice") String choice,
			@RequestParam("endpoint") String endpoint,HttpSession session){		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		byte[] check1;
		try {
			if (Utilities.isSessionValid(session, credentials)) {
				ResponseEntity<String> out = validateSourceCred(choice);
				System.out.println(new Gson().fromJson(out.getBody(),JsonObject.class).get("status").getAsString());
				if(new Gson().fromJson(out.getBody(),JsonObject.class).get("status").toString().equalsIgnoreCase("\"200\"")) {
					List<UrlObject> endpoints = credentials.getSrcObj().getDataEndPoints();
					int totalRows=0;
					Metering metring = new Metering();
					metring.setConnId(credentials.getCurrConnId().getConnectionId());
					metring.setTime(new Date()+"");
					metring.setType(choice);
					metring.setUserId(credentials.getUserId());
					for (UrlObject object : endpoints) {
						System.out.println("LABEL1" + object.getLabel());
						if (object.getLabel().trim().equalsIgnoreCase(endpoint.toLowerCase())) {
							Map<JsonElement,Integer> data1 = paginate(choice,object);
							
							Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
							JsonElement data=entry.getKey();
							Integer rows=entry.getValue();
							totalRows=rows;
							metring.setRowsFetched(object.getLabel().toLowerCase(), rows);
							metring.setTotalRowsFetched(totalRows);
							applicationEventPublisher.publishEvent(metring);
							
							String sheet="";
							switch(choice) {
								case "xml":{
									headers.setContentType(MediaType.APPLICATION_XML);
									JSONArray jobj = new JSONArray(data.toString());
									System.out.println(jobj.toString());
									sheet = XML.toString(jobj,"data");
									break;
								}
								case "json":{
									headers.setContentType(MediaType.APPLICATION_JSON);
									sheet = data.toString();
									break;
								}
								case "csv":{
									headers.setContentType(MediaType.TEXT_PLAIN);
									JFlat x = new JFlat(data.toString());
									List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
									sheet ="";
									for(Object[] row:json2csv) {
										for(Object element:row) {
											sheet+=String.valueOf(element)+",";
										}
										sheet = sheet.substring(0, sheet.length()-1);
										sheet+="\r\n";
									}
									break;
								}
							}					
							check1 = sheet.getBytes();					
							headers.add("Cache-Control", "no-cache");
							headers.add("access-control-allow-origin", config.getRootUrl());
							headers.add("access-control-allow-credentials", "true");
							headers.add("charset", "utf-8");
							headers.add("content-disposition", "attachment; filename=" +
									credentials.getCurrSrcName()+"_"+object.getLabel() +"."+choice);
							headers.add("Content-length",check1.length+"");
							System.out.println(headers);
							// return new ResponseEntity<byte[]>(output, responseHeaders, HttpStatus.OK)
							return new ResponseEntity<byte[]>(check1, headers, HttpStatus.OK);
						}
				}			
				}
				else {
					return ResponseEntity.status(HttpStatus.OK).headers(out.getHeaders()).body(out.getBody().getBytes());
				}
			}
			else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	private Map<JsonElement,Integer> paginate(String choice,UrlObject endpoint) {
		Map<JsonElement,Integer> response=new HashMap<>();
		try {
			
			HttpHeaders header = new HttpHeaders();
			ResponseEntity<String> out = null;
			RestTemplate restTemplate = new RestTemplate();
			Gson gson = new Gson();
			JsonArray mergedData = new JsonArray();
			JsonObject respBody = new JsonObject();
			//System.out.println("LABEL2" + endpoint.getLabel() + " " + credentials.getCurrConnId().getEndPoints());
			String url = buildUrl(endpoint, credentials.getSrcToken(), "DataController.fetchendpoint");
			System.out.println(endpoint.getLabel() + " = " + url);
			
			HttpHeaders headers = buildHeader(endpoint, credentials.getSrcToken(),
					"DataController.fetchendpoint");
			HttpEntity<?> httpEntity;
			if (!endpoint.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> preBody = buildBody(endpoint, credentials.getSrcToken(),
						"DataController.fetchendpoint");
				Object postBody = null;
				for (objects head : endpoint.getHeader()) {
					if (head.getKey().equalsIgnoreCase("content-type")) {
						postBody = bodyBuilder(head.getValue(), preBody,
								"DataController.fetchendpoint");
						break;
					}
				}
				httpEntity = new HttpEntity<Object>(postBody, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (endpoint.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
			System.out.println("Method : " + method);
			System.out.println(url);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			out = restTemplate.exchange(uri, method, httpEntity, String.class);

			Integer rows=0;
			
			if (choice.equalsIgnoreCase("view")) {
				System.out.println("View Data");
				respBody = new JsonObject();
				respBody.addProperty("status", "21");
				JFlat x = new JFlat(out.getBody());
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				String sheet ="";
				for(Object[] row:json2csv) {
					for(Object element:row) {
						sheet+=String.valueOf(element)+",";
					}
					sheet = sheet.substring(0, sheet.length()-1);
					sheet+="\r\n";
					if(json2csv.indexOf(row)>20)
						break;
				}
				respBody.addProperty("data", sheet);
				System.out.println(respBody.toString().substring(0, 20));
				response.put(respBody, rows);
				return response;
			} else {
				if (out.getBody() != null)
					mergedData.add(gson.fromJson(out.getBody().toString(), JsonElement.class));
				// call destination validation and push data
				// null and empty case+ three more cases+bodu cursor(dropbox).......and a lot
				// more

				System.out.println("\n--------------------------------------------------------------\n");

				System.out.println("While start");
				
				while (true) {

					String pData = null;
					String newurl = url;
					List<Cursor> page = endpoint.getPagination();
					if (page != null) {
						for (Cursor cur : page) {
							JsonObject ele = gson.fromJson(out.getBody(), JsonElement.class).getAsJsonObject();
							String arr[] = cur.getKey().split("::");
							for (String jobj : arr) {
								if (ele.get(jobj) != null && ele.get(jobj).isJsonObject()) {
									System.out.println(jobj);
									ele = ele.get(jobj).getAsJsonObject();
								} else {
									//System.out.println(ele.get(jobj));
									
									pData = ele.get(jobj) == null ? null : ele.get(jobj).getAsString();
									break;
								}
							}
							if (pData != null) {
								if (cur.getType().equalsIgnoreCase("url")) {
									newurl = pData;
								} else if (cur.getType().equalsIgnoreCase("append")) {
									newurl += newurl.contains("?") ? "&" + cur.getParam() + "=" + pData
											: "?" + cur.getParam() + "=" + pData;
									// newurl+="&"+cur.getParam()+"="+pData;
								} else {
									newurl += newurl.contains("?") ? "&" + cur.getParam() + "=" + pData
											: "?" + cur.getParam() + "=" + (Integer.parseInt(pData) + 1);
									// newurl+="&"+cur.getParam()+"="+Integer.parseInt(pData)+1;
								}
								System.out.println(newurl);
								break;
							}
						}
					}
					System.out.println(newurl);

					if (pData == null) {
						System.out.println("break pData");
						break;
					}
					uri = UriComponentsBuilder.fromUriString(newurl).build().encode().toUri();
					out = restTemplate.exchange(uri, method, httpEntity, String.class);
					
					if (out.getBody() == null) {
						System.out.println("break out.getBody");
						break;
					}
					else if (gson.fromJson(out.getBody(), JsonObject.class).get("data").getAsJsonArray().toString().equals("[]")) {
						System.out.println("break out.getBody.empty");
						break;
					}
					
					mergedData.add(gson.fromJson(out.getBody().toString(), JsonElement.class));
				}
				System.out.println("While End");
				System.out.println("\n--------------------------------------------------------------\n");
				// System.out.println(out.getBody());

				String outputData = mergedData.toString();
				JFlat x = new JFlat(outputData);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				rows=json2csv.size()-1;
				String tableName = credentials.getCurrConnId().getConnectionId() + "_" + endpoint.getLabel();
				if (choice.equalsIgnoreCase("export") && truncateAndPush(tableName)) {
					
					System.out.println("Export Data without schedule");
					

					System.out.println("SourceController-driver: " + credentials.getDestObj().getDrivers());

					if (pushDB(outputData, tableName, credentials.getDestObj(),credentials.getDestToken())) {						
						respBody.addProperty("status", "22");
						respBody.addProperty("data", "Successfullypushed");
					} else {
						respBody = new JsonObject();
						respBody.addProperty("status", "23");
						respBody.addProperty("data", "Unsuccessful");
					}
					response.put(respBody, rows);
					return response;
				}
				else {
					response.put(mergedData, rows);
					return response;
				}
			}
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObject respBody = new JsonObject();
		respBody.addProperty("status", "61");
		respBody.addProperty("data", "Error occured");
		response.put(respBody, 0);
		return response;
	}
	
	private boolean truncateAndPush(String tableName) {
		try {
			if (con == null || con.isClosed())
				connection(credentials.getDestToken(), credentials.getDestObj());
			PreparedStatement stmt;
			stmt = con.prepareStatement("SELECT count(*) AS COUNT FROM information_schema.tables WHERE table_name ="
					+ credentials.getDestObj().getValue_quote_open() + tableName
					+ credentials.getDestObj().getValue_quote_close() + ";");
			ResultSet res = stmt.executeQuery();
			res.first();
			System.out.println(res.getInt("COUNT"));
			if (res.getInt("COUNT") != 0) {
				stmt = con.prepareStatement("DROP TABLE " + tableName + ";");
				stmt.execute();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/checkconnection")
	public ResponseEntity<String> checkConnection(@RequestParam("choice") String choice,
			@RequestParam("connId") String connId, HttpSession httpsession) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		ResponseEntity<String> out = null;
		Gson gson = new Gson();
		try {
			boolean selectAction = false;
			JsonElement respBody = new JsonObject();
			System.out.println(credentials.getCurrConnId() + " "+ credentials.getDestObj()+" "+credentials.getSrcObj());
			if (Utilities.isSessionValid(httpsession, credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId())
						.setLastAccessTime(httpsession.getLastAccessedTime());
				if (credentials.getCurrConnId() == null) {
					System.out.println("currconId is null");
					credentials.setCurrDestValid(false);
					credentials.setCurrSrcValid(false);
					respBody.getAsJsonObject().addProperty("data", "DifferentAll");
					respBody.getAsJsonObject().addProperty("status", "13");
					return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
				} else if (credentials.getCurrConnId().getConnectionId().equalsIgnoreCase(connId)) {
					
					out = selectAction(choice, connId, httpsession);
					respBody = gson.fromJson(out.getBody(), JsonElement.class);
					selectAction=true;
				} else {
					if (credentials.getConnectionIds(connId).getSourceName()
							.equalsIgnoreCase(credentials.getCurrConnId().getSourceName())
							&& credentials.getConnectionIds(connId).getDestName()
									.equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {
						out = selectAction(choice, connId, httpsession);
						respBody = gson.fromJson(out.getBody(), JsonElement.class);
						selectAction=true;
					} else if (credentials.getConnectionIds(connId).getSourceName()
							.equalsIgnoreCase(credentials.getCurrConnId().getSourceName())) {
						credentials.setCurrDestValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentDestination");
						respBody.getAsJsonObject().addProperty("status", "12");
						return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
					} else if (credentials.getConnectionIds(connId).getDestName()
							.equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {
						credentials.setCurrSrcValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentSource");
						respBody.getAsJsonObject().addProperty("status", "11");
						return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
					} else {
						credentials.setCurrDestValid(false);
						credentials.setCurrSrcValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentAll");
						respBody.getAsJsonObject().addProperty("status", "13");
						return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
					}
				}				
				ConnObj currConnId = new ConnObj();
				currConnId.setDestName(credentials.getConnectionIds(connId).getDestName());
				currConnId.setSourceName(credentials.getConnectionIds(connId).getSourceName());
				currConnId.setEndPoints(credentials.getConnectionIds(connId).getEndPoints());
				currConnId.setConnectionId(connId);
				currConnId.setPeriod(credentials.getConnectionIds(connId).getPeriod());
				currConnId.setScheduled(credentials.getConnectionIds(connId).getScheduled());
				credentials.setConnectionIds(connId, currConnId);
				credentials.setCurrConnId(currConnId);
				if(selectAction) {
					if(!choice.equalsIgnoreCase("export"))
						headers=out.getHeaders();
					System.out.println("************");
					System.out.println(out.getBody());
					System.out.println(out.getHeaders().values()+""+out.getHeaders().getContentLength()+"");
				}
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				System.out.println("Session expired!");
				respBody = new JsonObject();
				respBody.getAsJsonObject().addProperty("message", "Sorry! Your session has expired");
				respBody.getAsJsonObject().addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}	

	@RequestMapping("/fetchdbs")
	private ResponseEntity<String> fetchDBs(@RequestParam("destId") String destId, HttpSession session) {
		ResponseEntity<String> out = null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId())
						.setLastAccessTime(session.getLastAccessedTime());
				String name = destId;
				String filter = "{\"_id\":{\"$regex\":\".*" + credentials.getUserId().toLowerCase() + "_"
						+ name.toLowerCase() + ".*\"}}";
				String url = config.getMongoUrl() + "/credentials/destinationCredentials?filter=" + filter;
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();

				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				RestTemplate restTemplate = new RestTemplate();
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				JsonObject respBody = new JsonObject();
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				respBody.addProperty("status", "200");
				respBody.add("data", obj.get("_embedded").getAsJsonArray());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (HttpClientErrorException e) {
			JsonObject respBody = new JsonObject();
			respBody.addProperty("data", "Error");
			respBody.addProperty("status", "404");
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
}
