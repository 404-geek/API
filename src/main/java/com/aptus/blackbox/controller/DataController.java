package com.aptus.blackbox.controller;

import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.event.ScheduleEventData;
import com.aptus.blackbox.index.ConnObj;
import com.aptus.blackbox.index.Cursor;
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.ScheduleInfo;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.index.objects;
import com.aptus.blackbox.utils.Utilities;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class DataController {
	private String  tableName;
	private Connection con = null;
	
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
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationContext Context;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	/*
	 * 
	 */
	private DestObject destObj;
	private Map<String,String> destToken;

	@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	private ResponseEntity<String> destination(HttpSession session,
			@RequestParam(value ="database_name") String database_name,
			@RequestParam(value ="db_username") String db_username,
			@RequestParam(value ="db_password") String db_password,
			@RequestParam(value ="server_host") String server_host,
			@RequestParam(value ="server_port") String server_port) throws SQLException { // @RequestParam("data") Map<String,String> data
		credentials.setCurrDestValid(false);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		try {			
			if(Utilities.isSessionValid(session,credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId()).setLastAccessTime(session.getLastAccessedTime());
				HashMap<String, String> destCred = new HashMap<>();
				destCred.put("database_name", database_name);
				destCred.put("db_username", db_username);
				destCred.put("db_password", db_password);
				destCred.put("server_host", server_host);
				destCred.put("server_port", server_port);
				//tableName = "user";
				credentials.setDestToken(destCred);			
				destObj = credentials.getDestObj();
				destToken = credentials.getDestToken();			
				if (!checkDB(destToken.get("database_name"),destToken,destObj)) {
					credentials.setCurrDestValid(false);
					System.out.println("Invalid database credentials");
					// invalid
				}
				credentials.setCurrDestValid(true);
				System.out.println("Database credentials validated");
				credentials.setDestToken(destCred);
				String url=homeUrl;
				headers.setLocation(URI.create(url+"/close.html"));				
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
			}
			else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(con!=null)con.close();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}	

	public boolean pushDB(String  jsonString, String tableName) throws SQLException {
		System.out.println("pushDBController-driver: "+destObj.getDrivers());
		this.tableName=tableName;
		try {
			System.out.println("TABLENAME: "+tableName);
			//System.out.println("JSONSTRING: "+jsonString);
			
			PreparedStatement preparedStmt;
			if (checkDB(destToken.get("database_name"),destToken,destObj)) {
				if (con == null || con.isClosed())
					connection(destToken,destObj);
				credentials.setCurrDestValid(true);
				JFlat x = new JFlat(jsonString);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				// System.out.println(json2csv);
				
				String statement = "";
				
				int i = 0;
				for (Object[] row : json2csv) {
					if (i == 0) {
						 statement="CREATE TABLE "+
								 destObj.getIdentifier_quote_open()+ tableName +destObj.getIdentifier_quote_close()+
								 "(";
						 for(Object t : row)
						 statement+=t.toString()+" TEXT,";
						
						 statement=statement.substring(0,statement.length()-1)+");";
						 System.out.println("-----"+statement);
						 preparedStmt = con.prepareStatement(statement);
						 preparedStmt.execute();
					} else {
						int k;						
						String instmt = "INSERT INTO " +
								 destObj.getIdentifier_quote_open()+ tableName +destObj.getIdentifier_quote_close()+
								  " VALUES(";
						
						for(k=0;k<row.length;k++)
							instmt+="?,";
						
						instmt = instmt.substring(0,instmt.length()-1)+");";
						PreparedStatement stmt = con.prepareStatement(instmt);
						
						k=1;
						for (Object attr : row) {
							stmt.setString( k++,attr==null?null:attr.toString());
						}
						//System.out.println(instmt);
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

	
	public void connection(Map<String,String> destToken,DestObject destObj) throws SQLException {
		try {
			
			System.out.println("DataController-driver: "+destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + destToken.get("server_host") + ":"
					+ destToken.get("server_port") + destObj.getDbnameseparator()
					+ destToken.get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, destToken.get("db_username"),
					destToken.get("db_password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkDB(String dbase,Map<String,String> destToken,DestObject destObj) throws SQLException {

		try {
			if (con == null || con.isClosed())
				connection(destToken, destObj);
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema =" 
			+ destObj.getValue_quote_open()+ dbase+destObj.getValue_quote_close()+";";
			System.out.println(query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next()) {
				con.close();
				return true;
			}
			con.close();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/selectaction")
	private ResponseEntity<String> selectAction(@RequestParam("choice") String choice,
			@RequestParam("connId") String connId,HttpSession httpsession) {
        ResponseEntity<String> ret = null;
        HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        try {
        	if(Utilities.isSessionValid(httpsession,credentials)) {
        		applicationCredentials.getApplicationCred().get(credentials.getUserId()).setLastAccessTime(httpsession.getLastAccessedTime());
        		credentials.setCurrConnId(credentials.getConnectionIds(connId));
        		if(credentials.getCurrConnId().getScheduled().equalsIgnoreCase("true") && choice.equalsIgnoreCase("export")) {        			
        			SchedulingObjects schObj=new SchedulingObjects();
        			schObj.setDestObj(credentials.getDestObj());
        			schObj.setDestToken(credentials.getDestToken());
        			schObj.setSrcObj(credentials.getSrcObj());
        			schObj.setSrcToken(credentials.getSrcToken());
        			schObj.setPeriod(credentials.getCurrConnId().getPeriod());
        			schObj.setNextPush(ZonedDateTime.now().toInstant().toEpochMilli());
        			schObj.setLastPushed(ZonedDateTime.now().toInstant().toEpochMilli());
        			schObj.setDestName(credentials.getCurrDestName());
        			schObj.setSrcName(credentials.getCurrSrcName());
        			for(String endpoint:credentials.getCurrConnId().getEndPoints()) {
        				schObj.setEndPointStatus(endpoint, null);
        			}
        			if(applicationCredentials.getApplicationCred().keySet().contains(credentials.getUserId())){
            			applicationCredentials.getApplicationCred().get(credentials.getUserId()).setSchedulingObjects(schObj, connId);
        			}
        			else {
        				ScheduleInfo scInfo = new ScheduleInfo();
            			scInfo.setSchedulingObjects(schObj, connId);        			
            			applicationCredentials.setApplicationCred(credentials.getUserId(), scInfo);
        			}  			
        			System.out.println("Publishing custom event. ");
        			 ScheduleEventData scheduleEventData=Context.getBean(ScheduleEventData.class);
        			 scheduleEventData.setData(credentials.getUserId(), connId,
        					 credentials.getCurrConnId().getPeriod());
        			 //Context.getAutowireCapableBeanFactory().autowireBean(scheduleEventData);
        			 applicationEventPublisher.publishEvent(scheduleEventData);
        			 
        			 JsonObject respBody = new JsonObject();
     				respBody.addProperty("status", "21");
     				respBody.addProperty("data","published");
     				return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
        		}
        		
        		else {        		
	        	SrcObject obj = credentials.getSrcObj();
	            if (obj.getRefresh().equals("YES")) {	            	
	                ret = Utilities.token(credentials.getSrcObj().getRefreshToken(),credentials.getSrcToken(),"DataController.Selectaction");
	                if (!ret.getStatusCode().is2xxSuccessful()) {	                	
	                    JsonObject respBody = new JsonObject();
	        			respBody.addProperty("message", "Re-authorize");
	    				respBody.addProperty("status", "51");
	    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());

	                } else {	                	
	                	//next piece of code is for saveValues 
	                	try {
	        				credentials.getSrcToken().putAll(new Gson().fromJson(ret.getBody(), HashMap.class));
	        			} catch (Exception e) {
	        				for (String s : ret.getBody().toString().split("&")) {
	        					System.out.println(s);
	        					credentials.getSrcToken().put(s.split("=")[0], s.split("=")[1]);
	        				}
	        			}
	                	applicationEventPublisher.publishEvent(new PushCredentials(credentials.getSrcObj(), credentials.getDestObj(),credentials.getSrcToken() , credentials.getDestToken(),
	    						credentials.getCurrSrcName(), credentials.getCurrDestName(), credentials.getUserId()));
	        			System.out.println("token : " + credentials.getSrcToken().keySet() + ":" + credentials.getSrcToken().values());
	                    ret = validateData(obj.getValidateCredentials(), obj.getEndPoints(),choice);
	                    return ret;
	                }
	            } else {
	                ret = Utilities.token(obj.getValidateCredentials(),credentials.getSrcToken(),"DataController.selectAction");
	                if (!ret.getStatusCode().is2xxSuccessful()) {
	                	credentials.setCurrSrcValid(false);
	                    JsonObject respBody = new JsonObject();
	        			respBody.addProperty("message", "Re-authorize");
	    				respBody.addProperty("status", "51");
	    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());

	                } else {
	                	credentials.setCurrSrcValid(true);
	                    //ret = Utilities.token(endPoints.get(0),credentials.getSrcToken());	                	
	                    ResponseEntity<String> out = fetchEndpointsData(obj.getEndPoints(),choice);
	                    System.out.println(out.getBody().substring(0, 20));
	                    return out;
	                }
	            }
	        }
        	}
        	else {
    			System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());
    		}
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e + "home.data");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
    }

    private ResponseEntity<String> validateData(UrlObject valid, List<UrlObject> endPoints, String choice) {
        ResponseEntity<String> ret = null;
        HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        try {
            ret = Utilities.token(valid,credentials.getSrcToken(),"DataController.validateData");
            if (!ret.getStatusCode().is2xxSuccessful()) {            	
                JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Contact Support");
				respBody.addProperty("status", "52");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(respBody.toString());
            } else {
            	
                return fetchEndpointsData(endPoints,choice);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("source.validatedata");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);
    }

    private ResponseEntity<String> fetchEndpointsData(List<UrlObject> endpoints, String choice)
    {
    	HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
    	ResponseEntity<String> out = null;
    		try {
    			Gson gson=new Gson();
    			JsonArray mergedData=new JsonArray();
    			RestTemplate restTemplate = new RestTemplate();
    			
    			for(UrlObject object:endpoints) {
    				System.out.println("LABEL1"+object.getLabel());
    				boolean value=credentials.getCurrConnId().getEndPoints().contains(object.getLabel().toLowerCase());
    				System.out.println(value+ " "+object.getLabel().toLowerCase());
    				
    				if(credentials.getCurrConnId().getEndPoints().contains(object.getLabel().toLowerCase())) {
    					System.out.println("LABEL2"+object.getLabel()+" "+credentials.getCurrConnId().getEndPoints());
    				String url = Utilities.buildUrl(object, credentials.getSrcToken(),"DataController.fetchendpoint");
        			System.out.println(object.getLabel() + " = " + url);

        			HttpHeaders headers = Utilities.buildHeader(object, credentials.getSrcToken(),"DataController.fetchendpoint");
        			HttpEntity<?> httpEntity;
        			if (!object.getResponseBody().isEmpty()) {
        				MultiValueMap<String, String> preBody = Utilities.buildBody(object, credentials.getSrcToken(),"DataController.fetchendpoint");
        				Object postBody=null;
        				for(objects head:object.getHeader())
        				{
        					if(head.getKey().equalsIgnoreCase("content-type")) {
        						postBody=Utilities.bodyBuilder(head.getValue(),preBody,"DataController.fetchendpoint");
        						break;
        					}
        				}
        				httpEntity = new HttpEntity<Object>(postBody, headers);
        			} else {
        				httpEntity = new HttpEntity<Object>(headers);
        			}
        			HttpMethod method = (object.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
        			System.out.println("Method : "+method);
        			System.out.println(url);
        			out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
        			
        			if(choice.equalsIgnoreCase("view")) {
        				System.out.println("View Data");
        	            JsonObject respBody = new JsonObject();
        				respBody.addProperty("status", "21");
        				respBody.add("data", gson.fromJson(out.getBody(), JsonElement.class));
        				System.out.println(respBody.toString().substring(0, 20));
        				return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
        			}
        			else if(choice.equalsIgnoreCase("export")){
        				System.out.println("Export Data without schedule");
        				if(out.getBody()!=null)
        				mergedData.add(new JsonParser().parse(out.getBody().toString()).getAsJsonObject());
        				//call destination validation and push data 
        			
                    //null and empty case+ three more cases+bodu cursor(dropbox).......and a lot more 
        			
        			System.out.println("\n--------------------------------------------------------------\n");
        			
        			System.out.println("While start");
        			while(true) {
        				
        				String pData=null;
        				String newurl = url;
        				List<Cursor> page =object.getPagination();
        				if(page!=null) {
        					for(Cursor cur:page) {
            					JsonObject ele = gson.fromJson(out.getBody(), JsonElement.class).getAsJsonObject();
            					String arr[] = cur.getKey().split("::");
            					for(String jobj:arr) {
            		                if(ele.get(jobj)!=null && ele.get(jobj).isJsonObject() )  {
            		                    System.out.println(jobj);
            		                    ele=ele.get(jobj).getAsJsonObject();
            		                }
            		                else {
            		                    System.out.println(ele.get(jobj));
            		                    pData = ele.get(jobj)==null?null:ele.get(jobj).getAsString();
            		                    break;
            		                }
            					}
            					if(pData!=null) {
            						if(cur.getType().equalsIgnoreCase("url")) {
            							newurl = pData;
            						}
            						else if(cur.getType().equalsIgnoreCase("append")) {
            							newurl+=newurl.contains("?")?"&"+cur.getParam()+"="+pData:"?"+cur.getParam()+"="+pData;
            							//newurl+="&"+cur.getParam()+"="+pData;
            						}
            						else {
            							newurl+=newurl.contains("?")?"&"+cur.getParam()+"="+pData:"?"+cur.getParam()+"="+(Integer.parseInt(pData)+1);
            							//newurl+="&"+cur.getParam()+"="+Integer.parseInt(pData)+1;
            						}
            						System.out.println(newurl);
            						break;
            					}	
            				}
        				}        				
        				System.out.println(newurl);
        				
        				if(pData==null) {
        					System.out.println("break pData");
        					break;
        				}
        				out = restTemplate.exchange(URI.create(newurl), method, httpEntity, String.class);
        				
        				if(out.getBody()==null) {
        					System.out.println("break out.getBody");
        					break;
        				}
        				mergedData.add(new JsonParser().parse(out.getBody().toString()).getAsJsonObject());
        				
        			}        	
        			System.out.println("While End");
        			System.out.println("\n--------------------------------------------------------------\n");
        			//System.out.println(out.getBody());
        			System.out.println("Merged Data "+mergedData.toString().substring(0, 30));
        			try (FileWriter writer = new FileWriter("Output.json")) {
        				
        			    Gson gsonb = new GsonBuilder().create();
        			    gsonb.toJson(mergedData, writer);
        			}
        			if(truncateAndPush()) {
        				String tableName=credentials.getCurrConnId().getConnectionId()+"_"+object.getLabel();

                        System.out.println("SourceController-driver: "+credentials.getDestObj().getDrivers());

                        
                        
                        if(pushDB(mergedData.toString(), tableName)) {
                        	JsonObject respBody = new JsonObject();
            				respBody.addProperty("status", "22");
            				respBody.addProperty("data", "Successfullypushed");
            				return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
                        } else {
              				 JsonObject respBody = new JsonObject();
             				respBody.addProperty("status", "23");
             				respBody.addProperty("data", "Unsuccessful");
             				return ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
            			}      	            
        			}
        			
    			}
    			}
    		}

    		} catch (Exception e) {
    			e.printStackTrace();
    			System.out.println(e+"token");
    		}
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(null);    	
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/checkconnection")
	private ResponseEntity<String>checkConnection(@RequestParam("choice") String choice,
			@RequestParam("connId") String connId,HttpSession httpsession)	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		ResponseEntity<String> out = null;
		Gson gson=new Gson();
		try {			
			JsonObject respBody = new JsonObject();
			System.out.println(credentials);
			if(Utilities.isSessionValid(httpsession,credentials)) {
				applicationCredentials.getApplicationCred().get(credentials.getUserId()).setLastAccessTime(httpsession.getLastAccessedTime());
				if(credentials.getCurrConnId()==null) {
					credentials.setCurrDestValid(false);
					credentials.setCurrSrcValid(false);
					respBody.addProperty("data", "DifferentAll");
					respBody.addProperty("status", "13");
				}
				else if(credentials.getCurrConnId().getConnectionId().equalsIgnoreCase(connId)) {
					out=selectAction(choice, connId, httpsession);
					respBody=gson.fromJson(out.getBody(), JsonElement.class).getAsJsonObject();					
				}
				else {
					if(credentials.getConnectionIds(connId).getSourceName().
							equalsIgnoreCase(credentials.getCurrConnId().getSourceName()) &&
					   credentials.getConnectionIds(connId).getDestName().
							equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {
						out=selectAction(choice, connId, httpsession);
						respBody=gson.fromJson(out.getBody(), JsonElement.class).getAsJsonObject();
					}
					else if(credentials.getConnectionIds(connId).getSourceName().
							equalsIgnoreCase(credentials.getCurrConnId().getSourceName())) {
						credentials.setCurrDestValid(false);
						respBody.addProperty("data", "DifferentDestination");
						respBody.addProperty("status", "12");
					}
					else if(credentials.getConnectionIds(connId).getDestName().
							equalsIgnoreCase(credentials.getCurrConnId().getDestName()))	{
						credentials.setCurrSrcValid(false);
						respBody.addProperty("data", "DifferentSource");
						respBody.addProperty("status", "11");
					}
					else {
						credentials.setCurrDestValid(false);
						credentials.setCurrSrcValid(false);
						respBody.addProperty("data", "DifferentAll");
						respBody.addProperty("status", "13");
					}
				}
				ConnObj currConnId = new ConnObj();
				currConnId.setDestName(credentials.getConnectionIds(connId).getDestName());
				currConnId.setSourceName(credentials.getConnectionIds(connId).getSourceName());
				currConnId.setEndPoints(credentials.getConnectionIds(connId).getEndPoints());
				currConnId.setConnectionId(connId);
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else {
				System.out.println("Session expired!");
    			respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
    
    private boolean truncateAndPush() {
    	try {
			if (con == null || con.isClosed())
				connection(destToken,destObj);
			PreparedStatement stmt;
			stmt = con.prepareStatement("SELECT count(*) AS COUNT FROM information_schema.tables WHERE table_schema =" 
					+ destObj.getValue_quote_open()+ credentials.getCurrConnId().getConnectionId()
					+destObj.getValue_quote_close()+";");
			ResultSet res = stmt.executeQuery();
			res.first();
			if(res.getInt("COUNT")!=0) {
				stmt = con.prepareStatement("TRUNCATE TABLE "+credentials.getCurrConnId().getConnectionId()+";");
				stmt.execute();
			}			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    @RequestMapping("/fetchdbs")
    private ResponseEntity<String> fetchDBs(@RequestParam("destId") String destId,HttpSession session){
    	ResponseEntity<String> out = null;
    	HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
    	try {
	    		if(Utilities.isSessionValid(session,credentials)) {
	        		applicationCredentials.getApplicationCred().get(credentials.getUserId()).setLastAccessTime(session.getLastAccessedTime());
	    			String name = destId;
	    			String filter = "{\"_id\":{\"$regex\":\".*"+name.toLowerCase() + ".*\"}}";					
	    			String url = mongoUrl+"/credentials/destinationCredentials?filter=" + filter;		 
	    			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
	    			
	    			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
	    			RestTemplate restTemplate = new RestTemplate();
	    			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
	    			JsonObject respBody = new JsonObject();
	    			JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
	    			respBody.addProperty("status", "200");
    				respBody.add("data", obj.get("_embedded").getAsJsonArray());
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
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
    }
}
