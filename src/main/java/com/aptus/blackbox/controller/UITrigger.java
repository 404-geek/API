package com.aptus.blackbox.controller;

import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.MeteringService;
import com.aptus.blackbox.dataServices.SchedulingService;
import com.aptus.blackbox.dataServices.UserConnectorService;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.ScheduleEventData;
import com.aptus.blackbox.event.Socket;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.Endpoint;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
public class UITrigger {


	
	@Autowired
	private Credentials credentials;	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private ApplicationContext Context;
	@Autowired
	private Config config;
	@Autowired
	private UserConnectorService userConnectorService;
	@Autowired
	private MeteringService meteringservice;
	@Autowired
	private SchedulingService schedulingService;
	
	//Functionality of below is unsure
	@RequestMapping(method = RequestMethod.GET, value = "/clientscheduledstatus")
    private ResponseEntity<String> getstatus(HttpSession session, @RequestParam("connId") String connId) {
        ResponseEntity<String> out = null;
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        
        try {         
        	if(Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
        	String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
			String url = config.getMongoUrl() + "/credentials/scheduleStatus?filter=" + filter;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            System.out.println("clientscheduledstatus");
            System.out.println(uri);
            HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			RestTemplate restTemplate = new RestTemplate();
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
			JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
			JsonObject respBody = new JsonObject();
			System.out.println("result:: "+obj);
            if(obj.get("_returned").getAsInt() == 0 ? false : true)
			{	
           
            JsonObject status= obj.getAsJsonObject().get(connId).getAsJsonObject();
            System.out.println(status);
				respBody.add("data",status);
				respBody.addProperty("status", "200");
				respBody.addProperty("message", "Scheduled Data Exist");
			}else {
				respBody.addProperty("data","null");
				respBody.addProperty("status", "200");
				respBody.addProperty("message", "Scheduled Data Not Exist");
			}
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
            
           
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
	
	
	
	
	
	
	
	@RequestMapping("/getScheduledStatus")
	public ResponseEntity<Object> getScheduledStatus(HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
				
				String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
				String url = config.getMongoUrl() + "/credentials/scheduleStatus?filter=" + filter;

				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();

				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				JsonObject respBody = new JsonObject();
				
				if(obj.get("_returned").getAsInt() == 0 ? false : true)
				{	
					respBody.add("data",obj);
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Exist");
				}else {
					respBody.addProperty("data","null");
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Not Exist");
				}
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers)
						.body(respBody.toString());
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		// store in credentials
	}
	
	
	

	@RequestMapping("/togglescheduling")
	public ResponseEntity<String> toggleScheduling(@RequestParam("connid") String connId,
			@RequestParam("toggle") String toggle,@RequestParam(value="period",required=false) String period,
			HttpSession session){
		HttpHeaders headers = new HttpHeaders();
		JsonObject respBody = new JsonObject();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		if (Utilities.isSessionValid(session, applicationCredentials,credentials.getUserId())) {
			if(credentials.getConnectionIds(connId).getSourceName().
						equalsIgnoreCase(credentials.getCurrConnObj().getSourceName()) &&
				   credentials.getConnectionIds(connId).getDestName().
						equalsIgnoreCase(credentials.getCurrConnObj().getDestName())) {				
				if(toggle.equalsIgnoreCase("on")) {
					boolean ret=false;
					ResponseEntity<String> out = null;
					RestTemplate restTemplate = new RestTemplate();
					//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
					String filter = "filter={\"_id\":\""+credentials.getUserId()+"\"}&filter={\""+connId+"\":{\"$exists\":true,\"$ne\":null}}";
					String url = config.getMongoUrl()+"/credentials/scheduledStatus?" + filter;
					URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
					HttpHeaders header = new HttpHeaders();
					// header.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
					HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
					out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
					System.out.println("UITrigger togglescheduling "+out.getBody());
					JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
					JsonObject jobj = jelem.getAsJsonObject();
					if(jobj.get("_returned").getAsInt() != 0) {
						JsonObject scheduledData = jobj.get("_embedded").getAsJsonArray()
								.get(0).getAsJsonObject().get(connId).getAsJsonObject();
						if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
							ConnObj connObj = new ConnObj();
		     				connObj.setConnectionId(connId);
		     				connObj.setDestName(credentials.getCurrDestName());
		     				connObj.setSourceName(credentials.getCurrSrcName());
		     				
		     				connObj.setSourceId(credentials.getCurrSrcId());
		     				connObj.setDestinationId(credentials.getCurrDestId());
		     				
		     				connObj.setPeriod(Integer.parseInt(period)*1000);
		     				
							SchedulingObjects schObj=new SchedulingObjects();
		        			schObj.setDestObj(credentials.getDestObj());
		        			schObj.setDestToken(credentials.getDestToken());
		        			schObj.setSrcObj(credentials.getSrcObj());
		        			schObj.setSrcToken(credentials.getSrcToken());
		        			schObj.setPeriod(Integer.parseInt(period)*1000);
		        			System.out.println(Integer.parseInt(period));
		        			schObj.setDestName(credentials.getCurrDestName());
		        			schObj.setSrcName(credentials.getCurrSrcName());
		        			schObj.setNextPush(ZonedDateTime.now().toInstant().toEpochMilli());
		        			schObj.setLastPushed(ZonedDateTime.now().toInstant().toEpochMilli());
		        			for(Entry<String, JsonElement> endpoint:scheduledData.entrySet()) {
		        				if(endpoint.getValue().isJsonObject()) {
		        					Map<String,Status> mp = new HashMap<>();
		        					Endpoint endp = new Endpoint();
		        					endp.setKey(endpoint.getKey());
		        					for(Entry<String, JsonElement>end : endpoint.getValue().getAsJsonObject().entrySet()) {
		        						if(end.getValue().isJsonObject()) {
		        							mp.put(end.getKey(), new Status("N.A","N.A"));
		        							endp.addValue(end.getKey());
		        						}
		        					}
		        					schObj.setEndPointStatus(endpoint.getKey().toString(), mp);
		        					connObj.setEndPoints(endp);
		        				}
		        			}
		        			connObj.setScheduled("true");
		        			credentials.setConnectionIds(connId, connObj);
							applicationCredentials.getApplicationCred().get(credentials.getUserId())
							.setSchedulingObjects(schObj, connId);
							ScheduleEventData scheduleEventData=Context.getBean(ScheduleEventData.class);
		        			 scheduleEventData.setData(credentials.getUserId(), connId,Integer.parseInt(period)*1000,true);
		        			 System.out.println(applicationCredentials.getApplicationCred().get(credentials.getUserId()).getSchedulingObjects().get(connId));
		        			 applicationEventPublisher.publishEvent(scheduleEventData);
						}
					}
				}
				else if(toggle.equalsIgnoreCase("off")) {
						if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
		            		if(applicationCredentials.getApplicationCred().get(credentials
		            				.getUserId()).getSchedulingObjects().get(connId)!=null) {
		            			applicationEventPublisher.publishEvent(new InterruptThread(applicationCredentials.getApplicationCred().get(credentials
		                				.getUserId()).getSchedulingObjects().get(connId).getThread()
		            					, true, credentials.getUserId(), credentials.getCurrConnObj().getConnectionId()));
		            			credentials.getConnectionIds(connId).setScheduled("false");
	            		}            		
	            	}
				}
				else if(toggle.equalsIgnoreCase("period")) {
					if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
	            		if(applicationCredentials.getApplicationCred().get(credentials
	            				.getUserId()).getSchedulingObjects().get(connId)!=null) {
	            			applicationCredentials.getApplicationCred().get(credentials.getUserId())
	            			.getSchedulingObjects().get(connId).getThread().cancel(false);
	            			applicationCredentials.getApplicationCred().get(credentials.getUserId())
	            			.getSchedulingObjects().get(connId).setPeriod(Integer.parseInt(period)*1000);
	            			ScheduleEventData scheduleEventData=Context.getBean(ScheduleEventData.class);
		        			 scheduleEventData.setData(credentials.getUserId(), connId,Integer.parseInt(period)*1000,true);
		        			 System.out.println(applicationCredentials.getApplicationCred().get(credentials.getUserId()).getSchedulingObjects().get(connId));
		        			 applicationEventPublisher.publishEvent(scheduleEventData);
		        			 credentials.getConnectionIds(connId).setPeriod(Integer.parseInt(period)*1000);
            		}            		
            	}
				}
				applicationEventPublisher.publishEvent(new Socket(credentials.getUserId()));	
			}
				else if(credentials.getConnectionIds(connId).getSourceName().
						equalsIgnoreCase(credentials.getCurrConnObj().getSourceName())) {
					credentials.setCurrDestValid(false);
					respBody.addProperty("data", "DifferentDestination");
					respBody.addProperty("status", "12");
				}
				else if(credentials.getConnectionIds(connId).getDestName().
						equalsIgnoreCase(credentials.getCurrConnObj().getDestName()))	{
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
			return null;			
		}
		else {
			System.out.println("Session expired!");
			respBody = new JsonObject();
			respBody.addProperty("message", "Sorry! Your session has expired");
			respBody.addProperty("status", "33");
			return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers)
					.body(respBody.toString());
		}
	}

	
	
	@RequestMapping(method = RequestMethod.GET, value = "/statistics")
	private ResponseEntity<String> getstats(/*@RequestParam("userId") String userId,*/HttpSession session) {
		
		HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        try{
        	JsonObject statistics= new JsonObject();
    		JsonObject obj = new JsonObject();
    		
        	if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
        		
        		System.out.println("=====datastats called");
        		obj=userConnectorService.countDataSourcesCreated(credentials.getUserId());//fetches count of files and ds created
        		obj.addProperty("RowsFetced", meteringservice.totalRows(credentials.getUserId()) );
        		obj.addProperty("DatasourcesScheduled", schedulingService.scheduleConnectionCount(credentials.getUserId()));
        		
        		//sources.add("No of Data sources created: ", userConnectorService.countDataSourcesCreated(userId));
        		//long a = meteringservice.totalRows(userId);
			
			
			}
        	else {
				session.invalidate();
					System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
        	
        	statistics.addProperty("code","200");
    		statistics.addProperty("message", "Statistics data updated");
    		statistics.add("data", obj);
    		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(statistics.toString());
					
        }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
		
	}
		
	
	
	
	

	@RequestMapping(method = RequestMethod.GET, value = "/resourceusage")
	private ResponseEntity<String> getusage(HttpSession session){
		HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		
		try {
			JSONObject details = new JSONObject();
			JSONObject disk = new JSONObject();
			JSONObject os = new JSONObject();
			JSONObject mem = new JSONObject();
			JSONArray arr = new JSONArray();
			File[] roots = File.listRoots();
			Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			os.put("OS name: ", System.getProperty("os.name"));
			os.put("OS version: ", System.getProperty("os.version"));
			os.put("OS architecture: ", System.getProperty("os.arch"));
			os.put("Available processors (cores): ", runtime.availableProcessors());
			details.put("OS info: ", os);
			
			mem.put("Free memory: ", format.format(runtime.freeMemory() / 1024));
			mem.put("Allocated memory: ", format.format(runtime.totalMemory() / 1024));
			mem.put("Max memory: ", format.format(runtime.maxMemory() / 1024));
			mem.put("Total free memory: ", format.format((runtime.freeMemory() + 
					(runtime.maxMemory() - runtime.totalMemory())) / 1024));
			details.put("Memory info: ", mem);
			
			for (File root : roots) {
				disk.put("File system root: ",root.getAbsolutePath());
				disk.put("Total space (bytes): ", root.getTotalSpace());
				disk.put("Free space (bytes): ", root.getFreeSpace());
				disk.put("Usable space (bytes): ", root.getUsableSpace());
				arr.put(disk);						
			}
			
			details.put("Disk info: ",arr);
			
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(details.toString());
			
		} 
		catch(HttpClientErrorException e) {
            JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Error");
            respBody.addProperty("status", "404");
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        }
		
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
		

	}
	
	

}
