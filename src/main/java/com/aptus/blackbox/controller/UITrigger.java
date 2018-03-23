package com.aptus.blackbox.controller;

import static org.mockito.Matchers.endsWith;

import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map.Entry;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.ScheduleEventData;
import com.aptus.blackbox.index.ConnObj;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
public class UITrigger {

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
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private ApplicationContext Context;
	
	
	@RequestMapping("/getScheduledStatus")
	public ResponseEntity<Object> getScheduledStatus(HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, credentials)) {
				
				String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
				String url = mongoUrl + "/credentials/scheduledStatus?filter=" + filter;

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
			@RequestParam("toggle") String toggle,@RequestParam("period") String period,HttpSession session){
		HttpHeaders headers = new HttpHeaders();
		JsonObject respBody = new JsonObject();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		if (Utilities.isSessionValid(session, credentials)) {
			if(credentials.getConnectionIds(connId).getSourceName().
						equalsIgnoreCase(credentials.getCurrConnId().getSourceName()) &&
				   credentials.getConnectionIds(connId).getDestName().
						equalsIgnoreCase(credentials.getCurrConnId().getDestName())) {				
				if(toggle.equalsIgnoreCase("on")) {
					boolean ret=false;
					ResponseEntity<String> out = null;
					RestTemplate restTemplate = new RestTemplate();
					//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
					String filter = "filter={\"_id\":\""+credentials.getUserId()+"\"}&filter={\""+connId+"\":{\"$exists\":true,\"$ne\":null}}";
					String url = mongoUrl+"/credentials/scheduledStatus?" + filter;
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
		        					schObj.setEndPointStatus(endpoint.getKey().toString(), null);
		        					connObj.setEndPoints(endpoint.getKey());
		        				}
		        			}
		        			connObj.setScheduled("true");
		        			credentials.setConnectionIds(connId, connObj);
							applicationCredentials.getApplicationCred().get(credentials.getUserId())
							.setSchedulingObjects(schObj, connId);
							ScheduleEventData scheduleEventData=Context.getBean(ScheduleEventData.class);
		        			 scheduleEventData.setData(credentials.getUserId(), connId,Integer.parseInt(period)*1000);
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
		            					, true, credentials.getUserId(), credentials.getCurrConnId().getConnectionId()));
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
		        			 scheduleEventData.setData(credentials.getUserId(), connId,Integer.parseInt(period)*1000);
		        			 System.out.println(applicationCredentials.getApplicationCred().get(credentials.getUserId()).getSchedulingObjects().get(connId));
		        			 applicationEventPublisher.publishEvent(scheduleEventData);
		        			 credentials.getConnectionIds(connId).setPeriod(Integer.parseInt(period)*1000);
            		}            		
            	}
				}
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

}
