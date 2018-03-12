package com.aptus.blackbox.threading;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.Cursor;
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

	
public class EndpointsTaskExecuter implements Runnable{

	private UrlObject endpoint;
	private String choice,connectionId;
	private  ResponseEntity<String> out;
	private DestObject destObj;
	private Map<String,String> destToken;
	
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	
	private Connection con = null;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsTaskExecuter.class);
	
	public  EndpointsTaskExecuter(UrlObject endpoints, String choice,String connectionId) {
		this.endpoint=endpoints;
		this.choice=choice;
		this.connectionId=connectionId;
		SchedulingObjects currObject = null;
		this.destObj=currObject.getDestObj();
		this.destToken = currObject.getDestToken();
	}
	

	public ResponseEntity<String> getOut() {
		return out;
	}


	public void setOut(ResponseEntity<String> out) {
		this.out = out;
	}
	
	@Override
	public void run() {
		RestTemplate restTemplate =new RestTemplate();
		Gson gson=new Gson();
		
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        
		try {
			String url = Utilities.buildUrl(endpoint, credentials.getCurrSrcToken());
			System.out.println(endpoint.getLabel() + " = " + url);

			HttpHeaders headers = Utilities.buildHeader(endpoint, credentials.getCurrSrcToken());
			HttpEntity<?> httpEntity;
			if (endpoint.getResponseString()!=null&&!endpoint.getResponseString().isEmpty()) {
				httpEntity = new HttpEntity<Object>(endpoint.getResponseString(), headers);
			} else if (!endpoint.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> body = Utilities.buildBody(endpoint, credentials.getCurrSrcToken());
				httpEntity = new HttpEntity<Object>(body, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (endpoint.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
			System.out.println("Method : "+method);
			System.out.println(url);
			out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
			
			//call destination validation and push data 
			
			//null and empty case+ three more cases+bodu cursor(dropbox).......and a lot more 
			
			System.out.println("\n--------------------------------------------------------------\n");
			
			while(true) {
				String pData=null;
				String newurl = url;
				List<Cursor> page =endpoint.getPagination();
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
			}        	
			System.out.println("\n--------------------------------------------------------------\n");
			//System.out.println(out.getBody());
			//System.out.println(out.getBody());
			
			if(choice.equalsIgnoreCase("view")) {				
			    JsonObject respBody = new JsonObject();
				respBody.addProperty("status", "21");
				respBody.add("data", gson.fromJson(out.getBody(), JsonElement.class));
				out = ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
				return;
			}
			else if(truncateAndPush()) {
				String tableName=credentials.getCurrConnId().getConnectionId()+"_"+endpoint.getLabel();

			    System.out.println("SourceController-driver: "+credentials.getCurrDestObj().getDrivers());

			    if(pushDB(out.getBody().toString(), tableName)) {
			    	JsonObject respBody = new JsonObject();
					respBody.addProperty("status", "22");
					respBody.addProperty("data", "Successfullypushed");
					out = ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
					return;
			    }        	            
			}
			else {
				 JsonObject respBody = new JsonObject();
					respBody.addProperty("status", "23");
					respBody.addProperty("data", "Unsuccessful");
					out =  ResponseEntity.status(HttpStatus.OK).headers(header).body(respBody.toString());
					return;
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
		out = ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(header).body(null);
		return;
	}
	
	public boolean pushDB(String  jsonString, String tableName) throws SQLException {
		System.out.println("pushDBController-driver: "+destObj.getDrivers());
		
		try {
			System.out.println("TABLENAME: "+tableName);
			//System.out.println("JSONSTRING: "+jsonString);
			
			String host = destToken.get("server_host");
			String port = destToken.get("server_port");
			String dbase = destToken.get("database_name");
			String user = destToken.get("db_username");
			String pass = destToken.get("db_password");
			PreparedStatement preparedStmt;
			if (checkDB(destToken.get("database_name"),destToken,destObj)) {
				if (con == null || con.isClosed())
					connection(destToken,destObj);
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
				credentials.getSchedulingObjects().get(connectionId).setDestValid(true);
				return true;
			}
			con.close();
			credentials.getSchedulingObjects().get(connectionId).setDestValid(false);
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		credentials.getSchedulingObjects().get(connectionId).setDestValid(false);
		return false;
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


}
