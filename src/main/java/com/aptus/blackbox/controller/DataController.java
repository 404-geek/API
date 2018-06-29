package com.aptus.blackbox.controller;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.BlackBoxReloadedApp;
import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.event.Metering;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.event.ScheduleEventData;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
	final Logger logger = LogManager.getLogger(BlackBoxReloadedApp.class.getPackage());
	 
	
	/*
	 * 
	 */

	//@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	public ResponseEntity<String> destination(String database_name,String db_username,String db_password,String server_host,String server_port) throws SQLException{
//			HttpSession session,
//			@RequestParam(value = "database_name") String database_name,
//			@RequestParam(value = "db_username") String db_username,
//			@RequestParam(value = "db_password") String db_password,
//			@RequestParam(value = "server_host") String server_host,
//			@RequestParam(value = "server_port") String server_port) throws SQLException { // @RequestParam("data")
//																							// Map<String,String> data
		credentials.setCurrDestValid(false);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			//if (Utilities.isSessionValid(session, credentials)) {
//				applicationCredentials.getApplicationCred().get(credentials.getUserId())
//						.setLastAccessTime(session.getLastAccessedTime());
				HashMap<String, String> destCred = new HashMap<>();
				destCred.put("database_name", database_name);
				destCred.put("db_username", db_username);
				destCred.put("db_password", db_password);
				destCred.put("server_host", server_host);
				destCred.put("server_port", server_port);
				// tableName = "user";
				credentials.setDestToken(destCred);
				logger.info("dest cred"+destCred);
				DestObject destObj = credentials.getDestObj();
				Map<String, String> destToken = credentials.getDestToken();
				
				System.out.println(checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("message").toString());
				
				if (!checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("status").getAsBoolean())
				{
					logger.info("dest valid"+false);
					credentials.setCurrDestValid(false);
					System.out.println("Invalid database credentials");
					JsonObject ret = new JsonObject();
					ret.addProperty("isvalid", false);
					URI uri = UriComponentsBuilder.fromUriString("/close.html").build().encode().toUri();
					headers.setLocation(uri);
					return ResponseEntity.status(HttpStatus.OK).headers(headers).body(ret.toString());					
					// invalid
				}
				
				else if(checkDB(destToken.get("database_name"), destToken, destObj).getAsJsonObject().get("status").getAsBoolean())
				{
					
					logger.info("dest valid"+true);
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
//			} else {
//				System.out.println("Session expired!");
//				JsonObject respBody = new JsonObject();
//				respBody.addProperty("message", "Sorry! Your session has expired");
//				respBody.addProperty("status", "33");
//				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
//			}
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
		JsonObject jobject = new JsonObject();
		jobject.addProperty("isvalid",false);
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobject.toString());
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
						int j=0;
						for (Object t : row) {
							String type;
							JsonPrimitive test = (JsonPrimitive) json2csv.get(1)[j];
							if(test.isNumber()) {
								type=destObj.getType_real();
							}
							else {
								type=destObj.getType_text();
							}
							statement += t.toString().replace("_", "") +" "+type+ ",";
						}

						statement = statement.substring(0, statement.length() - 1) + ");";
						System.out.println("-----" + statement);
						preparedStmt = con.prepareStatement(statement,ResultSet.TYPE_SCROLL_SENSITIVE, 
		                        ResultSet.CONCUR_UPDATABLE);
						preparedStmt.execute();
					} else {
						int k;
						String instmt = "INSERT INTO " + destObj.getIdentifier_quote_open() + tableName
								+ destObj.getIdentifier_quote_close() + " VALUES(";

						for (k = 0; k < row.length; k++)
							instmt += "?,";

						instmt = instmt.substring(0, instmt.length() - 1) + ");";
						PreparedStatement stmt = con.prepareStatement(instmt,ResultSet.TYPE_SCROLL_SENSITIVE, 
		                        ResultSet.CONCUR_UPDATABLE);

						k = 1;
						for (Object attr : row) {
							stmt.setString(k++, attr == null ? null : attr.toString());
						}
						System.out.println(instmt);
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
			System.out.println(url+" "+destToken.get("db_username")+" "+destToken.get("db_password"));
			con = DriverManager.getConnection(url, destToken.get("db_username"), destToken.get("db_password"));
		} 
		
		
		catch (SQLException  e) {
			
			System.out.println("inside connection sql exception");
			e.printStackTrace();
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
			e.printStackTrace();
			System.out.println(e.getErrorCode());
			System.out.println("check clientdatabase");
			resbody.addProperty("status", false);
			resbody.addProperty("message", "conncetion failed to client database");
			resbody.addProperty("code", "0");
			return resbody;
			// TODO Auto-generated catch block
			
		} catch (Exception e) {
			e.printStackTrace();
			resbody.addProperty("status", false);
			resbody.addProperty("message", "conncetion failed");
			resbody.addProperty("code", "500");
			return resbody;
			// TODO Auto-generated catch block
		}
		return resbody;
	}
	
	@RequestMapping(value="/graph")
	public ResponseEntity<String> graph(@RequestParam("user") String user,HttpSession httpsession){
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(httpsession, applicationCredentials,credentials.getUserId())) {
				RestTemplate restTemplate = new RestTemplate();
				String url = config.getMongoUrl()+"/credentials/metering/"+user.toLowerCase();
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				JsonObject MeteringCred  = new Gson().fromJson((restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody()),JsonObject.class);
				
				Map<String,Integer> dateRows = new HashMap<String,Integer>();
				
				Calendar cad = Calendar.getInstance();
				cad.setTime(new Date());
				
				ArrayList<String> viewDate = new ArrayList<>();
				ArrayList<Integer> data = new ArrayList<>();
				ArrayList<String> label = new ArrayList<>();
				
				for(int i=0;i<7;i++) {
					viewDate.add(new SimpleDateFormat("yyyy-MM-dd").format(cad.getTime()));
					cad.roll(Calendar.DATE, false);
				}
				
				
				for(Entry<String, JsonElement> conn:MeteringCred.entrySet()) {
					if(conn.getValue().isJsonObject() && conn.getValue().getAsJsonObject().keySet().contains("MeteringInfo")) {					
						for(JsonElement temp :conn.getValue().getAsJsonObject().get("MeteringInfo").getAsJsonArray()) {
							if(temp.isJsonObject()) {
								JsonObject temp1 = temp.getAsJsonObject();
								Date date = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy").parse(temp1.get("Time").getAsString());
								cad.setTime(date);
								
								String time = new SimpleDateFormat("yyyy-MM-dd").format(date);
								int totalRows = temp1.get("Total Rows").getAsInt();
								
								if(dateRows.containsKey(time)) {
									dateRows.put(time, dateRows.get(time)+totalRows);
								}
								else {
									dateRows.put(time, totalRows);		
								}
								
							}
						}
					}
				}
				
				Collections.sort(viewDate);
				
				for(String s:viewDate) {
					if(dateRows.containsKey(s)) {
						data.add(dateRows.get(s));
					}
					else {
						data.add(0);
					}
					label.add(new SimpleDateFormat("MMM dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(s)));
				}
				
				label.set(label.size()-1, "Today");
								
				
				System.out.println("\n"+dateRows+"\n"+data+"\n"+label);
				
				JsonObject ret = new JsonObject();
				ret.add("label", new Gson().toJsonTree(label));
				ret.addProperty("data", new Gson().toJson(data));
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(ret.toString());
			}
		}
		catch(Exception e) {
			
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/selectaction")
	public ResponseEntity<String> selectAction(@RequestParam("choice") String choice, HttpSession httpsession) {
		ResponseEntity<String> ret = null;
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(httpsession, applicationCredentials,credentials.getUserId())) {
				System.out.println(credentials.getCurrConnId() + " "+ credentials.getDestObj()+" "+credentials.getSrcObj());
				if(choice.equalsIgnoreCase("view")) {
					return validateSourceCred(choice);
				}
				else if (credentials.getCurrConnId().getScheduled().equalsIgnoreCase("true")
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
								.setSchedulingObjects(schObj, credentials.getCurrConnId().getConnectionId());
					} else {
						ScheduleInfo scInfo = new ScheduleInfo();
						scInfo.setSchedulingObjects(schObj, credentials.getCurrConnId().getConnectionId());
						applicationCredentials.setApplicationCred(credentials.getUserId(), scInfo);
					}
					System.out.println("Publishing custom event. ");
					ScheduleEventData scheduleEventData = Context.getBean(ScheduleEventData.class);
					scheduleEventData.setData(credentials.getUserId(), credentials.getCurrConnId().getConnectionId(), credentials.getCurrConnId().getPeriod(),true);
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
				ret = Utilities.token(credentials.getSrcObj().getRefreshToken(), credentials.getSrcToken(),
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
				ret = Utilities.token(obj.getValidateCredentials(), credentials.getSrcToken(),
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
			ret = Utilities.token(valid, credentials.getSrcToken(), "DataController.validateData");
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
			JsonObject data = new JsonObject();
			JsonArray endpoint = new JsonArray();
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
			Endpoint others = null;
			System.out.println(endp);
			for(Endpoint endpnt : credentials.getCurrConnId().getEndPoints()) {
				if(endpnt.getKey().equalsIgnoreCase("others")) {
					others = endpnt;
					
					for(UrlObject object:endp.get(endpnt.getKey().toLowerCase())) {
						
						System.out.println("LABEL1" + object.getLabel());
						boolean value = endpnt.getValue().contains(object.getLabel());
						System.out.println(value + " " + object.getLabel().toLowerCase());
						
						if(endpnt.getValue().contains(object.getLabel())){

								Map<JsonElement,Integer> data1 = paginate(choice,object);
								
								Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
								JsonObject datum=entry.getKey().getAsJsonObject();
								Integer rows=entry.getValue();
								if(!datum.get("status").toString().equalsIgnoreCase("21")) {
									success=false;
								}
								if(!choice.equalsIgnoreCase("view")) {
									totalRows+=rows;
									metring.setRowsFetched(object.getLabel().toLowerCase(), rows);
								}
								datum.addProperty("endpoint", object.getLabel());
								endpoint.add(datum);
							}
						
					}
				}
				else {
					UrlObject object = endp.get(endpnt.getKey().toLowerCase()).get(0);
					for(String endpntLable:endpnt.getValue()) {
						object.setLabel(endpntLable);
						Map<String,String> ne = new HashMap<>();
						ne.put(endpnt.getKey().toLowerCase(), endpntLable);
						object.setUrl(Utilities.url(object.getUrl(), ne));
						System.out.println("LABEL1" + object.getLabel());
						boolean value = endpnt.getValue().contains(object.getLabel());
						System.out.println(value + " " + object.getLabel().toLowerCase());
						
						if(endpnt.getValue().contains(object.getLabel())){

							Map<JsonElement,Integer> data1 = paginate(choice,object);
							
							Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
							JsonObject datum=entry.getKey().getAsJsonObject();
							Integer rows=entry.getValue();
							if(!datum.get("status").toString().equalsIgnoreCase("21")) {
								success=false;
							}
							
							if(!choice.equalsIgnoreCase("view")) {
								totalRows+=rows;
								metring.setRowsFetched(object.getLabel().toLowerCase(), rows);
							}
							datum.addProperty("endpoint", object.getLabel());
							endpoint.add(datum);
						}
					}
				}
			}
			
			List<String> infoendpnts = new ArrayList<>();
			for(UrlObject endpnt:credentials.getSrcObj().getInfoEndpoints()) {
				if(others.getValue().contains(endpnt.getLabel())) {
					infoendpnts.add(endpnt.getLabel());
				}
			}
			
			
			
			Map<JsonElement,Integer> ret = getInfoEndpoints(infoendpnts,choice);
			
			if(ret!=null) {
				if(choice.equalsIgnoreCase("view")) {
					JsonObject view = ret.entrySet().iterator().next().getKey().getAsJsonObject();
					for(Entry<String, JsonElement> end:view.entrySet()) {
						JsonObject temp = end.getValue().getAsJsonObject();
						temp.addProperty("endpoint", end.getKey());
						endpoint.add(temp);
					}
				}
				else {
					for(Entry<JsonElement, Integer> ent:ret.entrySet()) {
						totalRows+=ent.getValue();
						metring.setRowsFetched(ent.getKey().getAsString(),ent.getValue());
					}
				}
				
				
				metring.setTotalRowsFetched(totalRows);
				if(!choice.equalsIgnoreCase("view")) {
					applicationEventPublisher.publishEvent(metring);
				}
			}
			
			
			
			data.add("data", endpoint);
			data.addProperty("status", "21");
			data.addProperty("message", "succesful");
			return 	ResponseEntity.status(HttpStatus.OK).headers(header).body(data.toString());	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e + "token");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
	}

private Map<String,JsonElement> infoEndpointHelper(List<List<String>> infoEndpointOrder,Map<String,UrlObject> urlObjs,int pos, Map<String, List<String>> childrens, List<String> endpoins,String choice) {
		
		Map<String,JsonElement> ret = new HashMap<String,JsonElement>();
		try {
			if(pos == infoEndpointOrder.size())
				return ret;
			List<String> inf=infoEndpointOrder.get(pos) ;
			for(String key:inf) {
				//ResponseEntity<String> res=token(urlObjs.get(key),credentials.getSrcToken(), "infoEndpointHelper");
				
				Map<JsonElement, Integer> data1 = paginate("info", urlObjs.get(key));
				
				Map.Entry<JsonElement, Integer> entry=data1.entrySet().iterator().next();
				JsonElement element=entry.getKey();
				
				if(endpoins.contains(key)){
					if(ret.containsKey(key)) {
						JsonArray elem = ret.get(key).getAsJsonArray();elem.add(element);
						ret.put(key, elem);
					}
					else {
						ret.put(key, element);
					}
				}
				
				List<String> list = new ArrayList<String>();
				String arr[] = urlObjs.get(key).getData().split("::");
				list = Utilities.checkByPath(arr, 0, element, list);
				System.out.println("Datas");
				System.out.println(urlObjs.get(key).getLabel()+" : "+list);
				
				for(String id:list) {
					credentials.setSrcToken(urlObjs.get(key).getLabel(),id);
					ret.putAll(infoEndpointHelper(infoEndpointOrder,urlObjs,pos+1,childrens, endpoins,choice));					
					
				}
				
			}
			return ret;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return null;	
			
	}
	
	
	private Map<JsonElement,Integer> getInfoEndpoints(List<String> endpoints,String choice){
		
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", config.getRootUrl());
		header.add("access-control-allow-credentials", "true");
	
			try {
				List<String> endpoins = new ArrayList<>(endpoints);
				List<UrlObject> infoEndpoints = credentials.getSrcObj().getInfoEndpoints();	
				
				Map<String,List<String>> childrens = new HashMap<>();
				
				List<List<List<String>>> infoEndpointOrder = credentials.getSrcObj().getInfoEndpointOrder();
				
				if(infoEndpointOrder == null) {
					System.out.println("No Implicit Data Available");
					return null;

				}
				
				for(List<List<String>> lst:infoEndpointOrder) {
					List<String> ends = new ArrayList<>();
					for(int i = lst.size()-1;i>=0;i--) {
						for(String o:lst.get(i))
							childrens.put(o, ends);
						ends = new ArrayList<>(ends);
						ends.addAll(lst.get(i));
					}
				}
				
				System.out.println("childres:"+childrens);
				
				System.out.println("endpoins:"+endpoins);
				
				System.out.println("infoEndpointOrder:"+infoEndpointOrder);
				
				Map<String,UrlObject> urlObjs = new HashMap<>();
				infoEndpoints.forEach(e -> urlObjs.put(e.getLabel(), e));
				
				Map<String,JsonElement> elem = new HashMap<String,JsonElement>();
				
				for(List<List<String>> as:infoEndpointOrder) {
					System.out.println(childrens.get(as.get(0).get(0)));
					childrens.get(as.get(0).get(0)).forEach(o->{
						if(endpoins.contains(o)||endpoins.contains(as.get(0).get(0))) {
							System.out.println("inside");
							elem.putAll(infoEndpointHelper(as,urlObjs,0,childrens,endpoins,choice));
						}
					});					
				}
				
				System.out.println(elem);
				
				Map<JsonElement,Integer> ret = new HashMap<JsonElement,Integer>();
				
				
				
				JsonObject view = new JsonObject();
				for(Entry<String, JsonElement> ent:elem.entrySet()) {
					
					String outputData = ent.getValue().getAsJsonArray().toString();
					JFlat x = new JFlat(outputData);
					List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
					int rows=json2csv.size()-1;
					
					String tableName = credentials.getCurrConnId().getConnectionId() + "_" + ent.getKey();
						
					System.out.println("Export Data without schedule");						

					

					if(choice.equalsIgnoreCase("export")) {
						System.out.println("SourceController-driver: " + credentials.getDestObj().getDrivers());
						if (pushDB(outputData, tableName, credentials.getDestObj(),credentials.getDestToken())) {						
							ret.put(new JsonPrimitive(ent.getKey()), rows);
						} else {
							ret.put(new JsonPrimitive(ent.getKey()), -1);
						}
					}
					else if(choice.equalsIgnoreCase("download")) {
						ret.put(ent.getValue().getAsJsonArray(), rows);
					}
					else if(choice.equalsIgnoreCase("view")) {
						JsonObject respBody = new JsonObject();
						respBody.addProperty("status", "21");
						JsonArray array = new JsonArray();
						JsonArray columns = new JsonArray();
						int i=0;
						for(Object[] row:json2csv) {
							JsonArray ind = new JsonArray();
							int j=0;
							for(Object element:row) {
								if(i!=0) {
									ind.add(String.valueOf(element).replaceAll("\"", ""));
								}
								else {
									columns.add(String.valueOf(element).replaceAll("\"", ""));
								}								
							}
							if(i!=0)
								array.add(ind);
							i++;
							
							if(json2csv.indexOf(row)>20)
								break;
						}
						JsonObject ind = new JsonObject();
						ind.add("columns", columns);
						ind.add("data", array); 
						respBody.add("data", ind);
						System.out.println(respBody.toString().substring(0, 20));
						view.add(ent.getKey(), respBody);
					}
					
				}
				if(choice.equalsIgnoreCase("view")) {
					ret.put(view, 0);
				}
				
				System.out.println(ret);
				return ret;
								
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		
	}
	
	@RequestMapping("/fordownload")
	public ResponseEntity<JsonObject> fordownload(@RequestParam("data") JsonObject choice,HttpSession session){
		try {
			
			System.out.println(choice);
			JsonObject jobj = choice;
			JsonArray arr = new JsonArray();
			for(Entry<String, JsonElement> elem:jobj.entrySet()) {
				if(elem.getValue().isJsonObject()) {
					for(Entry<String, JsonElement> ele:elem.getValue().getAsJsonObject().entrySet()) {
						if(ele.getValue().isJsonPrimitive()) {
							if(ele.getValue().getAsString().equalsIgnoreCase("true"))
								arr.add(ele.getKey());;
						}
					}
				}
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
			headers.add("access-control-allow-credentials", "true");
			JsonObject respBody = new JsonObject();
			respBody.add("data", arr);
			respBody.addProperty("status", "200");
			respBody.addProperty("message", "endpoints");
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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
			if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
				ResponseEntity<String> out = validateSourceCred(choice);
				System.out.println(new Gson().fromJson(out.getBody(),JsonObject.class).get("status").getAsString());
				if(new Gson().fromJson(out.getBody(),JsonObject.class).get("status").getAsString().equalsIgnoreCase("200")) {
					System.out.println("inside If block");
					List<UrlObject> endpoints = credentials.getSrcObj().getDataEndPoints();
					int totalRows=0;
					Metering metring = new Metering();
					metring.setConnId(credentials.getCurrConnId().getConnectionId());
					
					metring.setTime(new Date()+"");
					metring.setType(choice);
					metring.setUserId(credentials.getUserId());
					Map<String,UrlObject> dataPoints = new HashMap<>();
					Map<String,UrlObject> infoData = new HashMap<>();
					for (UrlObject object : endpoints) {
						dataPoints.put(object.getLabel().toLowerCase(), object);
					}
					for(UrlObject object:credentials.getSrcObj().getInfoEndpoints()) {
						infoData.put(object.getLabel().toLowerCase(), object);
					}
					
					UrlObject object=null;
					Map<JsonElement,Integer> data1=null;
					System.out.println("dataPoints"+dataPoints);
					System.out.println("infoData"+infoData);
					System.out.println("endpoint"+endpoint);
					if(dataPoints.containsKey(endpoint.toLowerCase())) {
						object=dataPoints.get(endpoint.toLowerCase());
						data1 = paginate(choice,object);
					}
					else if(infoData.containsKey(endpoint.toLowerCase())) {
						object=infoData.get(endpoint.toLowerCase());
						List<String> end = new ArrayList<>();
						end.add(endpoint);
						data1=getInfoEndpoints(end, "download");
					}
					System.out.println(object);
					if(object!=null) {				
						
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
					else {
						return ResponseEntity.status(HttpStatus.OK).headers(headers).body("object is null".getBytes());
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
			String url = Utilities.buildUrl(endpoint, credentials.getSrcToken(), "DataController.fetchendpoint");
			System.out.println(endpoint.getLabel() + " = " + url);
			
			HttpHeaders headers = Utilities.buildHeader(endpoint, credentials.getSrcToken(),
					"DataController.fetchendpoint");
			HttpEntity<?> httpEntity;
			if (!endpoint.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> preBody = Utilities.buildBody(endpoint, credentials.getSrcToken(),
						"DataController.fetchendpoint");
				Object postBody = null;
				for (objects head : endpoint.getHeader()) {
					if (head.getKey().equalsIgnoreCase("content-type")) {
						postBody = Utilities.bodyBuilder(head.getValue(), preBody,
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
				JsonArray array = new JsonArray();
				JsonArray columns = new JsonArray();
				int i=0;
				for(Object[] row:json2csv) {
					JsonArray ind = new JsonArray();
					int j=0;
					for(Object element:row) {
						if(i!=0) {
							ind.add(String.valueOf(element).replaceAll("\"", ""));
						}
						else {
							columns.add(String.valueOf(element).replaceAll("\"", ""));
						}
					}
					if(i!=0)
						array.add(ind);
					i++;
					
					if(json2csv.indexOf(row)>20)
						break;
				}
				JsonObject ind = new JsonObject();
				ind.add("columns", columns);
				ind.add("data", array); 
				respBody.add("data", ind);
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

				if(!choice.equalsIgnoreCase("info")) {
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
				else {
					response.put(mergedData,0);
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
					+ credentials.getDestObj().getValue_quote_close() + ";",ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
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
			if (Utilities.isSessionValid(httpsession, applicationCredentials,credentials.getUserId())) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId())
						.setLastAccessTime(httpsession.getLastAccessedTime());
				if (credentials.getCurrConnId() == null) {
					System.out.println("currconId is null");
					credentials.setCurrDestValid(false);
					credentials.setCurrSrcValid(false);
					respBody.getAsJsonObject().addProperty("data", "DifferentAll");
					respBody.getAsJsonObject().addProperty("status", "13");
					//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
				} else if (credentials.getCurrConnId().getConnectionId().equalsIgnoreCase(connId)) {					
					out = selectAction(choice,  httpsession);
					respBody = gson.fromJson(out.getBody(), JsonElement.class);
					selectAction=true;
				} else {
					if (credentials.getConnectionIds(connId).getSourceName()
							.equalsIgnoreCase(credentials.getCurrConnId().getSourceName())
							&& credentials.getConnectionIds(connId).getDestName()
									.equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {
						out = selectAction(choice,  httpsession);
						respBody = gson.fromJson(out.getBody(), JsonElement.class);
						selectAction=true;
					} else if (credentials.getConnectionIds(connId).getSourceName()
							.equalsIgnoreCase(credentials.getCurrConnId().getSourceName())) {
						credentials.setCurrDestValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentDestination");
						respBody.getAsJsonObject().addProperty("status", "12");
						//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
					} else if (credentials.getConnectionIds(connId).getDestName()
							.equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {
						credentials.setCurrSrcValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentSource");
						respBody.getAsJsonObject().addProperty("status", "11");
						//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
					} else {
						credentials.setCurrDestValid(false);
						credentials.setCurrSrcValid(false);
						respBody.getAsJsonObject().addProperty("data", "DifferentAll");
						respBody.getAsJsonObject().addProperty("status", "13");
						//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
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
			if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
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