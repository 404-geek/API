package com.aptus.blackbox.event;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.threading.ConnectionsTaskScheduler;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class DataListeners {
	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;	
	@Autowired
	private ApplicationContext Context;


	@EventListener
	public void scheduleListner(ScheduleEventData scheduleEventData) {
		
		String userId=scheduleEventData.getUserId();
		String connId=scheduleEventData.getConnId();
		
		System.out.println("LISTENER THREAD START EndpointsTaskScheduler start");
		ConnectionsTaskScheduler connectionsTaskScheduler=Context.getBean(ConnectionsTaskScheduler.class);
		connectionsTaskScheduler.setConnectionsTaskScheduler(connId,userId);
		if(scheduleEventData.getScheduled().equalsIgnoreCase("true")){
			long period = scheduleEventData.getPeriod();
			ScheduledFuture<?> future = threadPoolTaskScheduler.scheduleAtFixedRate(connectionsTaskScheduler,period);
			applicationCredentials.getApplicationCred().get(userId).
    		getSchedulingObjects().get(connId).setThread(future);
			}
		else{
			threadPoolTaskScheduler.schedule(connectionsTaskScheduler,new Date());
		}

	}
	@EventListener
	public void statusListener(PostExecutorComplete result)
	{
		 String userId=result.getUserId();
		 String connId=result.getConnectionId();
		 pushStatus(connId, userId,"LISTENER THREAD END");
	}
	@EventListener
	public void interruptScheduler(InterruptThread thread)
	{
		 thread.getThread().cancel(true);
		 if(thread.isUserInterrupted()) {
			 applicationCredentials.getApplicationCred().get(thread.getUserId()).getSchedulingObjects().get(thread.getConnectionId()).setStatus("35");
			 applicationCredentials.getApplicationCred().get(thread.getUserId()).getSchedulingObjects().get(thread.getConnectionId()).setMessage("User Stopped Scheduling");
			 applicationEventPublisher.publishEvent(new PostExecutorComplete(thread.getUserId(),thread.getConnectionId()));
		 }		 
	}
	@EventListener
	public void updateCredentials(PushCredentials pushCredentials) {
		try {			
			// sourceCredentials
			JsonArray sourceBody = new JsonArray();
			for (Map.Entry<String, String> mp : pushCredentials.getSrcToken().entrySet()) {
				JsonObject tmp = new JsonObject();
				tmp.addProperty("key", String.valueOf(mp.getKey()));
				tmp.addProperty("value", String.valueOf(mp.getValue()));
				sourceBody.add(tmp);
			}
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("_id",
					pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getSrcName().toLowerCase());
			jsonObj.add("credentials", sourceBody);
			Utilities.postpatchMetaData(jsonObj, "source", "POST",pushCredentials.getUserId(),mongoUrl);
			// destCredentials
			JsonArray destBody =  new JsonArray();
			for (Map.Entry<String, String> mp : pushCredentials.getDestToken().entrySet()) {
				JsonObject tmp = new JsonObject();
				tmp.addProperty("key", String.valueOf(mp.getKey()));
				tmp.addProperty("value", String.valueOf(mp.getValue()));
				destBody.add(tmp);
			}
			jsonObj = new JsonObject();				
			jsonObj.addProperty("_id",
					pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getDestName().toLowerCase() + "_"
							+ pushCredentials.getDestToken().get("database_name"));
			jsonObj.add("credentials", destBody);
			Utilities.postpatchMetaData(jsonObj, "destination", "POST",pushCredentials.getUserId(),mongoUrl);
			
			System.out.println(sourceBody);
			System.out.println(destBody);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void pushStatus(String connectionId,String userId,String s)
	{
		System.out.println("UserId is "+userId+" connId is "+connectionId);
		System.out.println(s+applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getStatus()+" : "+applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getMessage());
		Iterator<Entry<String, Status>> entry=applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getEndPointStatus().entrySet().iterator();
		JsonObject connStatus = new JsonObject();
		JsonObject temp = new JsonObject();
		JsonObject endpointStatus = new JsonObject();
		while(entry.hasNext())
		{
			Entry<String, Status> e = entry.next();
			System.out.println(s + e.getKey()+" : "+e.getValue());			
			endpointStatus.addProperty("status", e.getValue().getStatus());
			endpointStatus.addProperty("messsage", e.getValue().getMessage());
			temp.add(e.getKey(), endpointStatus);
		}
		temp.addProperty("Last Succesfully Pushed", new Date(new Timestamp(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getLastPushed()).getTime())+"");
		temp.addProperty("Next Scheduled Pushed", new Date(new Timestamp(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getNextPush()).getTime())+"");
		temp.addProperty("status", applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getStatus());
		temp.addProperty("message", applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getMessage());
		connStatus.add(connectionId, temp);
		ResponseEntity<String> out = null;
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		String filter = "{\"_id\":\"" + userId.toLowerCase() + "\"}";
		String url;
		url = mongoUrl+"/credentials/scheduledStatus?filter=" + filter;
		URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
		out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
		System.out.println(s+out.getBody());
		JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		Boolean isPost=false;
		if(jobj.get("_returned").getAsInt() == 0) {
			connStatus.addProperty("_id", userId);
			isPost=true;
		}
		headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		httpEntity = new HttpEntity<Object>(connStatus.toString(),headers);
		if(isPost) {
			url = mongoUrl+"/credentials/scheduledStatus";
			uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			out = restTemplate.exchange(uri, HttpMethod.POST, httpEntity,String.class);
		}
		else {
			url = mongoUrl+"/credentials/scheduledStatus/"+userId;
			uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity,String.class);
		}
		System.out.println(connStatus.toString());
	}
}