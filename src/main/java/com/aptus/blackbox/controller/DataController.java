package com.aptus.blackbox.controller;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	
	private DestObject destObj;
	private Map<String,String> destToken;

	@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	private ResponseEntity<String> destination(HttpSession session) throws SQLException { // @RequestParam("data") Map<String,String> data
		try {
			if(Utilities.isSessionValid(session,credentials)) {
				HashMap<String, String> destCred = new HashMap<>();
				destCred.put("database_name", "test");
				destCred.put("db_username", "root");
				destCred.put("db_password", "blackbox");
				destCred.put("server_host", "192.168.1.36");
				destCred.put("server_port", "3306");
				//tableName = "user";
				credentials.setDestToken(destCred);			
				this.destObj = credentials.getDestObj();
				this.destToken = credentials.getDestToken();			
				if (!checkDB(destToken.get("database_name"))) {
					// invalid
				}
				HttpHeaders headers = new HttpHeaders();
				String url=homeUrl;
				headers.setLocation(URI.create(url+"/close.html"));
				headers.add("Cache-Control", "no-cache");
				headers.add("access-control-allow-origin", rootUrl);
				headers.add("access-control-allow-credentials", "true");
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);
			}
			else {
				System.out.println("Session expired!");
				HttpHeaders headers = new HttpHeaders();
				String url=homeUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			con.close();
		}
		return null;
	}
	
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
				JsonObject respBody = new JsonObject();
				respBody.addProperty("data", out.getBody());
				respBody.addProperty("status", "200");
				ConnObj conObj = new ConnObj();
				Gson gson=new Gson();
				JsonElement data = gson.fromJson(out.getBody(), JsonElement.class);
				JsonArray srcdestId = data.getAsJsonObject().get("srcdestId").getAsJsonArray();
				for(JsonElement ele:srcdestId) {
					conObj = gson.fromJson(ele, ConnObj.class);
					credentials.setConnectionIds(conObj.getConnectionId(), conObj);
				}
				System.out.println(credentials.getConnectionIds().values());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString().replace("\\\"", "\""));
			}
			else {
				System.out.println("Session expired!");
				String url=homeUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
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
		return null;
	}
		
	

	@RequestMapping(method = RequestMethod.POST, value = "/pushtodb")
	public boolean jsontodatabase(@RequestBody String  jsonString,@RequestParam("tableName") String tableName) throws SQLException {
		System.out.println("pushDBController-driver: "+destObj.getDrivers());
		this.tableName=tableName;
		try {
			if (con == null || con.isClosed())
				connection();
			System.out.println("TABLENAME: "+tableName);
			//System.out.println("JSONSTRING: "+jsonString);
			String host = destToken.get("server_host");
			String port = destToken.get("server_port");
			String dbase = destToken.get("database_name");
			String user = destToken.get("db_username");
			String pass = destToken.get("db_password");
			PreparedStatement preparedStmt;
			if (checkDB(destToken.get("database_name"))) {
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
						System.out.println(instmt);
						stmt.execute();
					}
					i++;
				}
				con.close();
				return true;
			}
		} catch (Exception e) {
			System.err.println("Got an exception!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
				
		
		return false;
	}

	
	public void connection() throws SQLException {
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

	public boolean checkDB(String dbase) throws SQLException {

		try {
			if (con == null || con.isClosed())
				connection();
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema =" 
			+ destObj.getValue_quote_open()+ dbase+destObj.getValue_quote_close()+";";
			System.out.println(query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next()) {
				credentials.setDestValid(true);
				return true;
			}
			credentials.setDestValid(false);
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
			@RequestParam String conId,HttpSession httpsession) {
        ResponseEntity<String> ret = null;
        try {
        	if(Utilities.isSessionValid(httpsession,credentials)) {
        		if(!credentials.getConnectionId().equalsIgnoreCase(conId)) {
        			//for same source n destination pair don't validate again
        			HttpHeaders headers = new HttpHeaders();
        			headers.add("Cache-Control", "no-cache");
        			headers.add("access-control-allow-origin", rootUrl);
                    headers.add("access-control-allow-credentials", "true");
                    JsonObject respBody = new JsonObject();
                    respBody.addProperty("data", "ConnChanged");
    				respBody.addProperty("status", "210");
    				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        		}
	        	SrcObject obj = credentials.getSrcObj();
	            if (obj.getRefresh().equals("YES")) {
	                ret = Utilities.token(credentials.getSrcObj().getRefreshToken(),credentials.getSrcToken());
	                if (!ret.getStatusCode().is2xxSuccessful()) {
	                	HttpHeaders header = new HttpHeaders();
	        			header.add("Cache-Control", "no-cache");
	        			header.add("access-control-allow-origin", rootUrl);
	                    header.add("access-control-allow-credentials", "true");
	                    JsonObject respBody = new JsonObject();
	        			respBody.addProperty("message", "Re-authorize");
	    				respBody.addProperty("status", "51");
	    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());
	//    				if (obj.getSteps().compareTo("TWO") == 0) {
	//    					ret = code(accessCode);
	//    				} else if (obj.getSteps().compareTo("THREE") == 0) {
	//    					ret = Utilities.token(requestToken,credentials.getSrcToken());
	//    					saveValues(ret);
	//    					ret = code(accessCode);
	//    				}
	                } else {
	                	//next code is saveValues 
	                	try {
	        				credentials.getSrcToken().putAll(new Gson().fromJson(ret.getBody(), HashMap.class));
	        			} catch (Exception e) {
	        				for (String s : ret.getBody().toString().split("&")) {
	        					System.out.println(s);
	        					credentials.getSrcToken().put(s.split("=")[0], s.split("=")[1]);
	        				}
	        			}
	        			System.out.println("token : " + credentials.getSrcToken().keySet() + ":" + credentials.getSrcToken().values());
	                    ret = validateData(obj.getValidateCredentials(), obj.getEndPoints(),choice);
	                }
	            } else {
	                ret = Utilities.token(obj.getValidateCredentials(),credentials.getSrcToken());
	                if (!ret.getStatusCode().is2xxSuccessful()) {
	                	credentials.setSrcValid(false);
	                	HttpHeaders header = new HttpHeaders();
	        			header.add("Cache-Control", "no-cache");
	        			header.add("access-control-allow-origin", rootUrl);
	                    header.add("access-control-allow-credentials", "true");
	                    JsonObject respBody = new JsonObject();
	        			respBody.addProperty("message", "Re-authorize");
	    				respBody.addProperty("status", "51");
	    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(header).body(respBody.toString());
	//    				if (obj.getSteps().compareTo("TWO") == 0) {
	//    					ret = code(accessCode);
	//    				} else if (obj.getSteps().compareTo("THREE") == 0) {
	//    					ret = Utilities.token(requestToken,credentials.getSrcToken());
	//    					saveValues(ret);
	//    					ret = code(accessCode);
	//    				}
	                } else {
	                	credentials.setSrcValid(true);
	                    //ret = Utilities.token(endPoints.get(0),credentials.getSrcToken());
	                	fetchEndpointsData(obj.getEndPoints(),choice);
	                    return ret;
	                }
	            }
	        }
        	else {
    			System.out.println("Session expired!");
    			HttpHeaders headers = new HttpHeaders();
    			String url=homeUrl;
    			headers.setLocation(URI.create(url));
    			return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
    		}
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e + "home.data");
        }
        return ret;
    }

    private ResponseEntity<String> validateData(UrlObject valid, List<UrlObject> endPoints, String choice) {
        ResponseEntity<String> ret = null;
        try {
            ret = Utilities.token(valid,credentials.getSrcToken());
            if (!ret.getStatusCode().is2xxSuccessful()) {
            	HttpHeaders header = new HttpHeaders();
    			header.add("Cache-Control", "no-cache");
    			header.add("access-control-allow-origin", rootUrl);
                header.add("access-control-allow-credentials", "true");
                JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Contact Support");
				respBody.addProperty("status", "52");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(header).body(respBody.toString());
            } else {
                //ret = Utilities.token(endPoints.get(0),credentials.getSrcToken());
            	fetchEndpointsData(endPoints,choice);
                return ret;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("source.validatedata");
        }
        return ret;
    }

    private ResponseEntity<String> fetchEndpointsData(List<UrlObject> endpoints, String choice)
    {
    	ResponseEntity<String> out = null;
    		try {
    			RestTemplate restTemplate = new RestTemplate();
    			for(UrlObject object:endpoints) {
    				String url = Utilities.buildUrl(object, credentials.getSrcToken());
        			System.out.println(object.getLabel() + " = " + url);

        			HttpHeaders headers = Utilities.buildHeader(object, credentials.getSrcToken());
        			HttpEntity<?> httpEntity;
        			if (object.getResponseString()!=null&&!object.getResponseString().isEmpty()) {
        				httpEntity = new HttpEntity<Object>(object.getResponseString(), headers);
        			} else if (!object.getResponseBody().isEmpty()) {
        				MultiValueMap<String, String> body = Utilities.buildBody(object, credentials.getSrcToken());
        				httpEntity = new HttpEntity<Object>(body, headers);
        			} else {
        				httpEntity = new HttpEntity<Object>(headers);
        			}
        			HttpMethod method = (object.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
        			System.out.println("Method : "+method);
        			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
        			System.out.println("----------------------------"+uri);
        			out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
        			
        			//call destination validation and push data 
        			
   ////null and empty case+ three more cases+bodu cursor(dropbox).......and a lot more     			
//        			while(true) {
//        				List<Cursor> page =object.getPagination();
//        				JsonObject ele = new Gson().fromJson(out.getBody(), JsonElement.class).getAsJsonObject();
//        				for(Cursor cur:page) {
//        					String arr[] = cur.getKey().split(".");
//        					for(String jobj:arr)
//        						JsonElement je =  
//        				}
//        			}
        			
        			//System.out.println(out.getBody());
        			 //System.out.println(out.getBody());
        			headers = new HttpHeaders();
    				headers.add("Cache-Control", "no-cache");
    				headers.add("access-control-allow-origin", rootUrl);
    	            headers.add("access-control-allow-credentials", "true");
        			if(choice.equalsIgnoreCase("view")) {
        				
        	            JsonObject respBody = new JsonObject();
        				respBody.addProperty("status", "211");
        				respBody.addProperty("data", out.getBody().toString());
        				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        			}
        			else {
        				String tableName=credentials.getConnectionId()+"_"+object.getLabel();

                        System.out.println("SourceController-driver: "+credentials.getDestObj().getDrivers());

                        url = "http://localhost:8080/pushtodb?tableName="+tableName;

                        httpEntity = new HttpEntity<Object>(out.getBody(),null);

                        ResponseEntity<Boolean> res= restTemplate.exchange(URI.create(url),
                                HttpMethod.POST,httpEntity,Boolean.class);
                        
        	            JsonObject respBody = new JsonObject();
        				respBody.addProperty("status", "212");
        				respBody.addProperty("data", "Successfullypushed");
        				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        			}
    			}
    			

    		} catch (Exception e) {
    			e.printStackTrace();
    			System.out.println(e+"token");
    		}
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    }



}
