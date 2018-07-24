package com.aptus.blackbox.event;

import java.net.URI;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataServices.SrcDestCredentialsService;
import com.aptus.blackbox.datamodels.SrcDestCredentials;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.models.MeteredEndpoints;
import com.aptus.blackbox.threading.ConnectionsTaskScheduler;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Component
public class DataListeners {
	@Autowired
	private Config config;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;	
	@Autowired
	private ApplicationContext Context;

	private SimpMessagingTemplate template;
	@Autowired
	public DataListeners(SimpMessagingTemplate template) {
		this.template = template;
	}
	
	@EventListener
	public void changeSocket(Socket socket) {
		
		String user = socket.getUser();
		Gson gson = new Gson();
    	
    	RestTemplate restTemplate = new RestTemplate();
		HttpHeaders header = new HttpHeaders();
		HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
		
		String url = config.getMongoUrl()+"/credentials/userCredentials?filter={\"_id\":\""+user.toLowerCase()+"\"}";
		URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();

		JsonObject UserCred  = gson.fromJson((restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody()),JsonObject.class);
		
		url = config.getMongoUrl()+"/credentials/metering/"+user.toLowerCase();
		uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();

		JsonObject MeteringCred  = gson.fromJson((restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody()),JsonObject.class);
		
		int numDataSources=0,
		scheduledDataSources=0,
		numDownloaded=0,
		numRows=0;
		
		if(UserCred.get("_returned").getAsInt()==0) {
			numDataSources=0;
			scheduledDataSources=0;
			numDownloaded=0;
			numRows=0;
		}
		else {
			JsonObject embed = UserCred.get("_embedded").getAsJsonArray().get(0).getAsJsonObject();
			for(JsonElement srcdest:embed.get("srcdestId").getAsJsonArray()) {
				if(srcdest.isJsonObject()) {
					JsonObject temp = srcdest.getAsJsonObject();
					numDataSources++;
					if(temp.get("scheduled").getAsString().equalsIgnoreCase("true")) {
						scheduledDataSources++;
					}					
				}
			}
			numRows = MeteringCred.get("Total rows").getAsInt();
			for(Entry<String, JsonElement> conn:MeteringCred.entrySet()) {
				if(conn.getValue().isJsonObject() && conn.getValue().getAsJsonObject().keySet().contains("MeteringInfo")) {					
					for(JsonElement temp :conn.getValue().getAsJsonObject().get("MeteringInfo").getAsJsonArray()) {
						if(temp.isJsonObject()) {
							if(!temp.getAsJsonObject().get("Type").getAsString().equalsIgnoreCase("export")) {
								for(JsonElement e:temp.getAsJsonObject().get("Endpoints").getAsJsonArray())
									numDownloaded++;
							}
						}
					}
				}
			}
		}
		JsonObject ret = new JsonObject();
		ret.addProperty("numDataSources", numDataSources);
		ret.addProperty("scheduledDataSources", scheduledDataSources);
		ret.addProperty("numDownloaded", numDownloaded);
		ret.addProperty("numRows", numRows);
		
		
    	this.template.convertAndSend("/client/message",ret.toString());
	}

	@EventListener
	public void scheduleListner(ScheduleEventData scheduleEventData) {		
		try {
			String userId=scheduleEventData.getUserId();
			String connId=scheduleEventData.getConnId();		
			System.out.println("LISTENER THREAD START EndpointsTaskScheduler start");
			ConnectionsTaskScheduler connectionsTaskScheduler=Context.getBean(ConnectionsTaskScheduler.class);
			connectionsTaskScheduler.setConnectionsTaskScheduler(connId,userId);
			long period = scheduleEventData.getPeriod();
			ScheduledFuture<?> future;
			if(!scheduleEventData.isFirst()) {
				future = threadPoolTaskScheduler.schedule(connectionsTaskScheduler, new Date(ZonedDateTime.now().toInstant().toEpochMilli()+period));
			}
			else {
				 future = threadPoolTaskScheduler.scheduleAtFixedRate(connectionsTaskScheduler,period);
			}
			applicationCredentials.getApplicationCred().get(userId).
			getSchedulingObjects().get(connId).setThread(future);
		} catch (BeansException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	@EventListener
	public void statusListener(PostExecutorComplete result)
	{
		 try {
			String userId=result.getUserId();
			 String connId=result.getConnectionId();
			 pushStatus(connId, userId,"LISTENER THREAD END");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@EventListener
	public void interruptScheduler(InterruptThread thread)
	{
		try {
			applicationCredentials.getApplicationCred().get(thread.getUserId()).getSchedulingObjects().get(thread.getConnectionId()).setNextPush(0);
			 thread.getThread().cancel(false);
			 if(thread.isUserInterrupted()) {
				 applicationCredentials.getApplicationCred().get(thread.getUserId()).getSchedulingObjects().get(thread.getConnectionId()).setStatus("35");
				 applicationCredentials.getApplicationCred().get(thread.getUserId()).getSchedulingObjects().get(thread.getConnectionId()).setMessage("User Stopped Scheduling");
				 pushStatus(thread.getConnectionId(), thread.getUserId(), "User Stopped Scheduling");
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Autowired
	private SrcDestCredentialsService srcDestCredentialsService;
	
	
	@EventListener
	public void updateCredentials(PushCredentials pushCredentials) {			
			JsonObject jsonObj = new JsonObject();
			
		
			try {
				
				List<Map<String,String>> mSrcDestCred;
				SrcDestCredentials srcDestCredentials;
				
				// sourceCredentials
				if((pushCredentials.getSrcName()!=null)&&(pushCredentials.getSrcToken()!=null)&&(pushCredentials.getSrcObj()!=null))
				{
					JsonArray sourceBody = new JsonArray();
					mSrcDestCred =  new ArrayList<Map<String,String>>();
					
					for (Map.Entry<String, String> mp : pushCredentials.getSrcToken().entrySet()) {
						JsonObject tmp = new JsonObject();
						

						Map<String, String > map = new HashMap<String,String>();
						map.put("key", String.valueOf(mp.getKey()));
						map.put("value", String.valueOf(mp.getValue()));
						mSrcDestCred.add(map);
						
						
						tmp.addProperty("key", String.valueOf(mp.getKey()));
						tmp.addProperty("value", String.valueOf(mp.getValue()));
						sourceBody.add(tmp);
					}
					srcDestCredentials  = new SrcDestCredentials();
					srcDestCredentials.setCredentialId(pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getSrcName().toLowerCase());
					srcDestCredentials.setCredentials(mSrcDestCred);
					System.out.println(srcDestCredentials);
					srcDestCredentialsService.insertCredentials(srcDestCredentials, Constants.COLLECTION_SOURCECREDENTIALS);
					
					jsonObj.addProperty("_id",
							pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getSrcName().toLowerCase());
					jsonObj.add("credentials", sourceBody);
					Utilities.postpatchMetaData(jsonObj, "source", "POST",pushCredentials.getUserId(),config.getMongoUrl());
					System.out.println(sourceBody);
				}
				// destCredentials
				if((pushCredentials.getDestName()!=null)&&(pushCredentials.getDestToken()!=null)&&(pushCredentials.getDestObj()!=null)) {
					JsonArray destBody =  new JsonArray();
					mSrcDestCred =  new ArrayList<Map<String,String>>();
					
					for (Map.Entry<String, String> mp : pushCredentials.getDestToken().entrySet()) {
						JsonObject tmp = new JsonObject();
						
						
						
						Map<String, String > map = new HashMap<String,String>();
						map.put("key", String.valueOf(mp.getKey()));
						map.put("value", String.valueOf(mp.getValue()));
						mSrcDestCred.add(map);
						
						tmp.addProperty("key", String.valueOf(mp.getKey()));
						tmp.addProperty("value", String.valueOf(mp.getValue()));
						destBody.add(tmp);
					}
					jsonObj = new JsonObject();				
					
					
					srcDestCredentials  = new SrcDestCredentials();
					srcDestCredentials.setCredentialId(pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getDestName().toLowerCase()+"_"+
							pushCredentials.getDestToken().get("database_name"));
					srcDestCredentials.setCredentials(mSrcDestCred);
					System.out.println(srcDestCredentials);
					srcDestCredentialsService.insertCredentials(srcDestCredentials, Constants.COLLECTION_DESTINATIONCREDENTIALS);
					
					jsonObj.addProperty("_id",
							pushCredentials.getUserId().toLowerCase() + "_" + pushCredentials.getDestName().toLowerCase() + "_"
									+ pushCredentials.getDestToken().get("database_name"));
					jsonObj.add("credentials", destBody);
					Utilities.postpatchMetaData(jsonObj, "destination", "POST",pushCredentials.getUserId(),config.getMongoUrl());
					System.out.println(destBody);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public void pushStatus(String connectionId,String userId,String s)
	{
		try {
			SchedulingObjects tempScheduleObj = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
			System.out.println("UserId is "+userId+" connId is "+connectionId);
			System.out.println(s+tempScheduleObj.getStatus()+" : "+tempScheduleObj.getMessage());
			Iterator<Entry<String, Map<String, Status>>> entry=tempScheduleObj.getEndPointStatus().entrySet().iterator();
			JsonObject connStatus = new JsonObject();
			JsonObject temp = new JsonObject();
			while(entry.hasNext())
			{
				JsonObject catagoryStatus = new JsonObject();
				Entry<String, Map<String, Status>> e = entry.next();
				System.out.println(s + e.getKey()+" : "+e.getValue());
				Iterator<Entry<String, Status>> itr = e.getValue().entrySet().iterator();
				System.out.println("loop1");
				while(itr.hasNext()) {
					JsonObject endpointStatus = new JsonObject();
					Entry<String, Status> it = itr.next();
					endpointStatus.addProperty("status", it.getValue().getStatus());
					endpointStatus.addProperty("messsage", it.getValue().getMessage());
					catagoryStatus.add(it.getKey(), endpointStatus);
					System.out.println("\tloop2");
				}				
				temp.add(e.getKey(), catagoryStatus);
			}
			String value = new Date(new Timestamp(tempScheduleObj.getNextPush()).getTime())+"";
			temp.addProperty("Last Succesfully Pushed", new Date(new Timestamp(tempScheduleObj.getLastPushed()).getTime())+"");
			temp.addProperty("status", tempScheduleObj.getStatus());
			temp.addProperty("message", tempScheduleObj.getMessage());
			if(tempScheduleObj.getNextPush()==0) {
				value = "N.A";	
				System.out.println(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().remove(connectionId)+"");
				System.out.println(applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId)+"");
			}			
			temp.addProperty("Next Scheduled Pushed", value);
			connStatus.add(connectionId, temp);
			ResponseEntity<String> out = null;
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			String filter = "{\"_id\":\"" + userId.toLowerCase() + "\"}";
			String url;
			url = config.getMongoUrl()+"/credentials/scheduledStatus?filter=" + filter;
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
				url = config.getMongoUrl()+"/credentials/scheduledStatus";
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				out = restTemplate.exchange(uri, HttpMethod.POST, httpEntity,String.class);
			}
			else {
				url = config.getMongoUrl()+"/credentials/scheduledStatus/"+userId;
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity,String.class);
			}
			System.out.println(connStatus.toString());
		} 
		catch(JsonSyntaxException e) {
			e.printStackTrace();
		}
		catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e) {
			
		}
	}
	@EventListener
	private void pushMeteringInfo(Metering metering) {
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Content-Type", "application/json");
        Gson gson = new Gson();
		try {
			System.out.println("-----------Pushing Metering Data--------started-------");
			ResponseEntity<String> out = null;
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			String filter = "{\"_id\":\"" + metering.getUserId() + "\"}";
			String url;
			url = config.getMongoUrl()+"/credentials/metering?filter=" + filter;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();		
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println(out.getBody());
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			JsonObject time = new JsonObject();
			time.addProperty("Total Rows", metering.getTotalRowsFetched());
			time.addProperty("Type", metering.getType());	
			time.addProperty("Time", metering.getTime());
			JsonArray endPoints = new JsonArray();
			System.out.println(out.getBody());
			for(Entry<String, MeteredEndpoints> temp:metering.getRowsFetched().entrySet()) {
				JsonObject endPoint = new JsonObject();
				endPoint.addProperty("name", temp.getKey());
				endPoint.addProperty("rows", temp.getValue().getNumRecords());
				endPoints.add(endPoint);
			}
			time.add("Endpoints", endPoints);
			System.out.println(out.getBody());
			if(jobj.get("_returned").getAsInt() == 0 ? false : true) {
				url = config.getMongoUrl()+"/credentials/metering/"+metering.getUserId().toLowerCase();
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();		
				httpEntity = new HttpEntity<Object>(headers);
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
				System.out.println(out.getBody().toString());
				int TotalRows = gson.fromJson(out.getBody(), JsonObject.class).get("Total rows").getAsInt();
				JsonObject addtoset=new JsonObject();
				JsonArray each=new JsonArray();
				each.add(time);
				if(gson.fromJson(out.getBody(), JsonObject.class).get(metering.getConnId())!=null) {
					int numRows = gson.fromJson(out.getBody(), JsonObject.class).get(metering.getConnId())
							.getAsJsonObject().get("Total rows").getAsInt();
					System.out.println(numRows);
					numRows+=metering.getTotalRowsFetched();
					
					System.out.println("if"+out.getBody());
						
					
					System.out.println(each);
					JsonObject e=new JsonObject();
					e.add("$each", each);
					System.out.println(e);
					JsonObject f=new JsonObject();
					f.add(metering.getConnId()+"."+"MeteringInfo", e);
					System.out.println(f);
					addtoset.add("$addToSet", f);
					addtoset.addProperty(metering.getConnId()+"."+"Total rows", numRows);
					addtoset.addProperty("Total rows", TotalRows+metering.getTotalRowsFetched());					
				}
				else {
					JsonObject connId = new JsonObject();
					connId.add("MeteringInfo", each);
					connId.addProperty("Total rows", metering.getTotalRowsFetched());
					addtoset.add(metering.getConnId(), connId);
					addtoset.addProperty("Total rows",TotalRows+metering.getTotalRowsFetched());
				}
				System.out.println(addtoset);
				url = config.getMongoUrl()+"/credentials/metering/"+metering.getUserId();
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				httpEntity = new HttpEntity<Object>(addtoset.toString(),headers);
				out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity,String.class);
			}
			else {
				System.out.println("else"+out.getBody());
				JsonArray meteringInfo=new JsonArray();
				meteringInfo.add(time);				
				JsonObject upper = new JsonObject();				
				upper.addProperty("_id", metering.getUserId());				
				JsonObject connId=new JsonObject();
				connId.add("MeteringInfo", meteringInfo);
				connId.addProperty("Total rows", metering.getTotalRowsFetched());
				upper.add(metering.getConnId(), connId);
				upper.addProperty("Total rows", metering.getTotalRowsFetched());
				httpEntity = new HttpEntity<Object>(upper.toString(),headers);
				url = config.getMongoUrl()+"/credentials/metering";
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();	
				out = restTemplate.exchange(uri, HttpMethod.POST, httpEntity,String.class);
			}
			System.out.println("-----------Pushing Metering Data--------ended-------");
			applicationEventPublisher.publishEvent(new Socket(metering.getUserId()));
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
