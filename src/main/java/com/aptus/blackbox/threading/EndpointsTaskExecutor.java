package com.aptus.blackbox.threading;

import java.net.URI;
import java.sql.Timestamp;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.PostExecutorComplete;
import com.aptus.blackbox.event.ScheduleEventData;
import com.aptus.blackbox.DestinationAuthorisation;
import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.models.Cursor;
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

@Component
@Scope("prototype")
public class EndpointsTaskExecutor extends RESTFetch implements Runnable{
	
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private boolean bool;
	private List<String> endpoints;
	private UrlObject endpoint;
	private String connectionId,userId,catagory;
	private Status result;
	private SchedulingObjects scheduleObject;	
	private Connection con = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsTaskExecutor.class);
	
	public void setEndpointsTaskExecutor(List<String> endpoints,UrlObject endpoint, String connectionId,String user,Thread parent,String catagory,boolean bool) {
		this.endpoints = endpoints;
		this.catagory = catagory;
		this.endpoint=endpoint;
		this.connectionId=connectionId;
		this.userId = user;
		this.bool=bool;
		this.scheduleObject = applicationCredentials.getApplicationCred().get(user).getSchedulingObjects().get(connectionId);
	}
	
	public void setResult(Status result) {
		this.result = result;
		System.out.println(Thread.currentThread().getName()+"SET RESULT \t"+applicationCredentials.getApplicationCred());
						System.out.println(Thread.currentThread().getName()+"SET RESULT \t"+applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects()
								.get(connectionId).getEndPointStatus());
								System.out.println(Thread.currentThread().getName()+"SET RESULT \t"+endpoint);
		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects()
		.get(connectionId).getEndPointStatus().get(catagory).put(endpoint.getLabel(),result);
		
		scheduleObject = applicationCredentials.getApplicationCred().get(userId)
				.getSchedulingObjects().get(connectionId);
		
		for(String e:scheduleObject.getstatus())
			System.out.println(e);
		
		if(!scheduleObject.getstatus().contains("31")) {
			if(!scheduleObject.getstatus().contains("32")) {
				long time = ZonedDateTime.now().toInstant().toEpochMilli();
				if(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getNextPush()==0) {
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setStatus("35");
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setMessage("User Stopped Scheduling");
				}
				else {
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setStatus("33");
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setMessage("Completed Successfully");
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId)
					.setNextPush(time+scheduleObject.getPeriod());
					ScheduleEventData scheduleEventData = new ScheduleEventData();
					scheduleEventData.setData(userId, connectionId, scheduleObject.getPeriod(),false);
					applicationEventPublisher.publishEvent(scheduleEventData);
				}				
				applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId)
				.setLastPushed(time);
				applicationEventPublisher.publishEvent(new PostExecutorComplete(userId,connectionId));
				System.out.println("THREAD	EXECUTOR setResult"+new Date(new Timestamp(time).getTime()));				
				applicationEventPublisher.publishEvent(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering());
			}
			else {
				applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setStatus("32");
				applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setMessage("One or more endpoints encountered error");
				applicationEventPublisher.publishEvent(new InterruptThread(scheduleObject.getThread(),false, userId, connectionId));
			}
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR setResult " + 
			applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getEndPointStatus().keySet());
			
		}
	}
	
	@Override
	public void run() {
		
		if(bool) {
			paginate(endpoint);
		}
		else {
			getInfoEndpoints();
		}
		
		return;
	}
	
	private JsonElement paginate(UrlObject endpoint) {
		System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+endpoint.getLabel());
		RestTemplate restTemplate =new RestTemplate();
		Gson gson=new Gson();
		
		HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        
		try {
			String url = Utilities.buildUrl(endpoint, scheduleObject.getSrcToken(),Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+endpoint.getLabel() + " = " + url);

			HttpHeaders headers = Utilities.buildHeader(endpoint, scheduleObject.getSrcToken(),Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			HttpEntity<?> httpEntity;
			if (!endpoint.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> preBody = Utilities.buildBody(endpoint, scheduleObject.getSrcToken(),"THREAD	EXECUTOR RUN");
				Object postBody=null;
				for(objects head:endpoint.getHeader())
				{
					if(head.getKey().equalsIgnoreCase("content-type")) {
						postBody=Utilities.bodyBuilder(head.getValue(),preBody,"THREAD	EXECUTOR RUN");
						break;
					}
				}
				httpEntity = new HttpEntity<Object>(postBody, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (endpoint.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+"Method : "+method);
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+url);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			ResponseEntity<String> out = restTemplate.exchange(uri, method, httpEntity, String.class);
			
			//call destination validation and push data 
			
			//null and empty case+ three more cases+bodu cursor(dropbox).......and a lot more 
			
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+"\n--------------------------------------------------------------\n");
			JsonArray mergedData = new JsonArray();
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
								System.out.print("inside if...");
								System.out.println("jobj is "+jobj);
								ele = ele.get(jobj).getAsJsonObject();
							} else {
								System.out.print("inside else...");
								System.out.println("ele.get(jobj): "+ele.get(jobj));
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
							System.out.println("newurl is: "+newurl);
							break;
						}
					}
				}
				System.out.println("new url is:  "+newurl);

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
//				else if (gson.fromJson(out.getBody(), JsonObject.class).get("data").getAsJsonArray().toString().equals("[]")) {
//					System.out.println("break out.getBody.empty");
//					break;
//				}
				
				mergedData.add(gson.fromJson(out.getBody().toString(), JsonElement.class));
			}
			System.out.println("While End");
			System.out.println("\n--------------------------------------------------------------\n");
	        	
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN\n--------------------------------------------------------------\n");
			if(!bool) {
				return mergedData;
			}
			else {
				String tableName=connectionId+"_"+endpoint.getLabel();
				String outputData = mergedData.toString();
				
				JFlat x = new JFlat(outputData);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				
				int rows=json2csv.size()-1;
							
				System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN table "+tableName);	
				if(truncate(tableName)) {				
	
				    System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN SourceController-driver: "+scheduleObject.getDestObj().getDrivers());
	
				    if(pushDB(outputData, tableName)) {
				    	Status respBody = new Status("22","successfully pushed");
				    	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering().setRowsFetched(endpoint.getCatagory().toLowerCase(),endpoint.getLabel().toLowerCase(), rows);
						applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering()
						.setTotalRowsFetched(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering().getTotalRowsFetched() + rows);
	
						setResult(respBody);
						
						return null;
				    }        	            
				} 
				else {
					Status respBody = new Status("23","unsuccessful");
					setResult(respBody);
						return null;
				}
			}
		}
		catch(HttpClientErrorException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN"+e.getMessage());
			e.printStackTrace();
		}
		catch (RestClientException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			e.printStackTrace();
			// TODO Auto-generated catch block
		} catch (JsonSyntaxException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			e.printStackTrace();
			// TODO Auto-generated catch block
		} catch (NumberFormatException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			e.printStackTrace();
			// TODO Auto-generated catch block
		} catch (SQLException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			e.printStackTrace();
			// TODO Auto-generated catch block
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN");
			e.printStackTrace();
		}
		
		Status respBody = new Status("32","error");
		setResult(respBody);
		return null;
	}
	
	private Map<String,JsonElement> infoEndpointHelper(List<List<String>> infoEndpointOrder,Map<String,UrlObject> urlObjs,int pos, Map<String, List<String>> childrens) {
		
		Map<String,JsonElement> ret = new HashMap<String,JsonElement>();
		try {
			if(pos == infoEndpointOrder.size())
				return ret;
			List<String> inf=infoEndpointOrder.get(pos) ;
			for(String key:inf) {
				//ResponseEntity<String> res=token(urlObjs.get(key),credentials.getSrcToken(), "infoEndpointHelper");
				
				JsonElement element = paginate(urlObjs.get(key));
				
				if(endpoints.contains(key)){
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
					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setSrcToken(urlObjs.get(key).getLabel(),id);
					ret.putAll(infoEndpointHelper(infoEndpointOrder,urlObjs,pos+1,childrens));					
					
				}
				
			}
			return ret;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return null;	
			
	}
	
	
	private Map<JsonElement,Integer> getInfoEndpoints(){
	
			try {
				
				List<UrlObject> infoEndpoints = scheduleObject.getSrcObj().getInfoEndpoints();	
				
				Map<String,List<String>> childrens = new HashMap<>();
				
				List<List<List<String>>> infoEndpointOrder = scheduleObject.getSrcObj().getInfoEndpointOrder();
				
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
				
				System.out.println(Thread.currentThread().getName()+"childres:"+childrens);
				
				System.out.println(Thread.currentThread().getName()+"endpoins:"+endpoints);
				
				System.out.println(Thread.currentThread().getName()+"infoEndpointOrder:"+infoEndpointOrder);
				
				Map<String,UrlObject> urlObjs = new HashMap<>();
				infoEndpoints.forEach(e -> urlObjs.put(e.getLabel(), e));
				
				Map<String,JsonElement> elem = new HashMap<String,JsonElement>();
				
				for(List<List<String>> as:infoEndpointOrder) {
					System.out.println(childrens.get(as.get(0).get(0)));
					childrens.get(as.get(0).get(0)).forEach(o->{
						if(endpoints.contains(o)||endpoints.contains(as.get(0).get(0))) {
							System.out.println("inside");
							elem.putAll(infoEndpointHelper(as,urlObjs,0,childrens));
						}
					});					
				}
				
				System.out.println(elem);
				
				
				
				
				for(Entry<String, JsonElement> ent:elem.entrySet()) {
					
					String outputData = ent.getValue().getAsJsonArray().toString();
					JFlat x = new JFlat(outputData);
					List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
					this.endpoint = urlObjs.get(ent.getKey());
					int rows=json2csv.size()-1;
					
					String tableName=connectionId+"_"+ent.getKey();
					
					System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN table "+tableName);	
					if(truncate(tableName)) {				
		
					    System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR RUN SourceController-driver: "+scheduleObject.getDestObj().getDrivers());
		
					    if(pushDB(outputData, tableName)) {
					    	Status respBody = new Status("22","successfully pushed");
					    	
					    	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering().setRowsFetched(ent.getKey().toLowerCase(), rows);
							applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering()
							.setTotalRowsFetched(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getMetering().getTotalRowsFetched() + rows);
		
							setResult(respBody);
							
							return null;
					    }        	            
					} 
					else {
						Status respBody = new Status("23","unsuccessful");
						setResult(respBody);
							return null;
					}
										
					
				}
				
								
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Status respBody = new Status("32","error");
			setResult(respBody);
			return null;
		
	}
	
	public boolean pushDB(String  jsonString, String tableName) throws SQLException {
		System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB pushDBController-driver: "+scheduleObject.getDestObj().getDrivers());
		
		try {
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB TABLENAME: "+tableName);
			//System.out.println("JSONSTRING: "+jsonString);
			
			String host = scheduleObject.getDestToken().get("server_host");
			String port = scheduleObject.getDestToken().get("server_port");
			String dbase = scheduleObject.getDestToken().get("database_name");
			String user = scheduleObject.getDestToken().get("db_username");
			String pass = scheduleObject.getDestToken().get("db_password");
			PreparedStatement preparedStmt;
			if (checkDB(dbase,scheduleObject.getDestToken(),scheduleObject.getDestObj())) {
				if (con == null || con.isClosed())
					connection(scheduleObject.getDestToken(),scheduleObject.getDestObj());
				JFlat x = new JFlat(jsonString);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				// System.out.println(json2csv);
				
				String statement = "";
				
				int i = 0;
				for (Object[] row : json2csv) {
					if (i == 0) {
						 statement="CREATE TABLE "+
								 scheduleObject.getDestObj().getIdentifier_quote_open()+ tableName +scheduleObject.getDestObj().getIdentifier_quote_close()+
								 "(";
						 for(Object t : row)
								statement += t.toString().replace("_", "") + " TEXT,";
						
						 statement=statement.substring(0,statement.length()-1)+");";
						 System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB-----"+statement);
						 preparedStmt = con.prepareStatement(statement);
						 preparedStmt.execute();
					} else {
						int k;						
						String instmt = "INSERT INTO " +
								scheduleObject.getDestObj().getIdentifier_quote_open()+ tableName +scheduleObject.getDestObj().getIdentifier_quote_close()+
								  " VALUES(";
						
						for(k=0;k<row.length;k++)
							instmt+="?,";
						
						instmt = instmt.substring(0,instmt.length()-1)+");";
						PreparedStatement stmt = con.prepareStatement(instmt);
						
						k=1;
						for (Object attr : row) {
							stmt.setString( k++,attr==null?null:attr.toString());
						}
						System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB"+instmt);
						stmt.execute();
					}
					i++;
				}
				con.close();
				return true;
			}
			} catch (Exception e) {
			System.err.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB Got an exception!");
			System.err.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB");
			e.printStackTrace();
		}		
		return false;
	}

	
	public void connection(Map<String,String> destToken,DestinationConfig destObj) throws SQLException {
		try {
			
			System.out.println(Thread.currentThread().getName()+"THREAD	EXECUTOR CONNECTION DataController-driver: "+destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + destToken.get("server_host") + ":"
					+ destToken.get("server_port") + destObj.getDbnameseparator()
					+ destToken.get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, destToken.get("db_username"),
					destToken.get("db_password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB Got an exception!");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(Thread.currentThread().getName()+"THREAD	EXECUTOR PUSHDB Got an exception!");
			e.printStackTrace();
		}
	}

	public boolean checkDB(String dbase,Map<String,String> destToken,DestinationConfig destObj) throws SQLException {

		try {
			if (con == null || con.isClosed())
				connection(destToken, destObj);
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema =" 
			+ destObj.getValue_quote_open()+ dbase+destObj.getValue_quote_close()+";";
			System.out.println(Thread.currentThread().getName()+"THREAD EXECUTOR CHECKDB"+query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next()) {
				con.close();
				applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setDestValid(true);
				return true;
			}
			con.close();
			applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setDestValid(true);
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(Thread.currentThread().getName()+"THREAD EXECUTOR CHECKDB");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(Thread.currentThread().getName()+"THREAD EXECUTOR CHECKDB");
			e.printStackTrace();
		}		
		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setDestValid(false);
		return false;
	}
	
	private boolean truncate(String tableName) {
    	try {
			if (con == null || con.isClosed())
				connection(scheduleObject.getDestToken(),scheduleObject.getDestObj());
			PreparedStatement stmt;
			stmt = con.prepareStatement("SELECT count(*) AS COUNT FROM information_schema.tables WHERE table_name =" 
					+ scheduleObject.getDestObj().getValue_quote_open()+ tableName
					+scheduleObject.getDestObj().getValue_quote_close()+";");
			System.out.println(stmt);
			ResultSet res = stmt.executeQuery();
			res.first();
			System.out.println(res.getInt("COUNT"));
			if(res.getInt("COUNT")!=0) {
				stmt = con.prepareStatement("DROP TABLE "+tableName+";");
				System.out.println(stmt);
				stmt.execute();
			}			
			return true;
		} catch (SQLException e) {
			System.out.println(Thread.currentThread().getName()+"THREAD EXECUTOR TRUNCATE");
			e.printStackTrace();
		}
    	catch (Exception e) {
    		System.out.println(Thread.currentThread().getName()+"THREAD EXECUTOR TRUNCATE");
    		e.printStackTrace();
    	}
    	return false;
    }


}
