package com.aptus.blackbox.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.threading.ConnectionsTaskScheduler;

@Component
public class DataListeners {
	@Autowired
	private ApplicationCredentials applicationCredentials;

	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	@Autowired
	private ApplicationContext Context;


	@EventListener
	public void scheduleListner(ScheduleEventData scheduleEventData) {
		
		String userId=scheduleEventData.getUserId();
		String connId=scheduleEventData.getConnId();
		
		System.out.println("EndpointsTaskScheduler start");
		ConnectionsTaskScheduler connectionsTaskScheduler=Context.getBean(ConnectionsTaskScheduler.class);
		connectionsTaskScheduler.setConnectionsTaskScheduler(connId,userId);
		if(scheduleEventData.getScheduled().equalsIgnoreCase("true")){
			long period = Integer.parseInt(scheduleEventData.getPeriod())*1000;
			threadPoolTaskScheduler.scheduleAtFixedRate(connectionsTaskScheduler,period);
			}
		else{
			threadPoolTaskScheduler.schedule(connectionsTaskScheduler,new Date());
		}
		
		applicationCredentials.getApplicationCred().get(userId).
		getSchedulingObjects().get(connId).setMessage("In progress");
		
		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connId).setStatus("Progress CODE");
		
	
		for(String endpt:applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connId).getEndPointStatus().keySet())
		{
			applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
			get(scheduleEventData.getConnId()).setEndPointStatus(endpt, new Status("31","Running"));;
		}
		
		pushStatus(connId, userId);

	}
	
	
	public void pushStatus(String connectionId,String userId)
	{
		System.out.println("UserId is "+userId+" connId is "+connectionId);
		Iterator<Entry<String, Status>> entry=applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
				get(connectionId).getEndPointStatus().entrySet().iterator();
		while(entry.hasNext())
		{
			Entry<String, Status> e = entry.next();
			System.out.println(e.getKey()+" : "+e.getValue());
		}
		
	}
	@EventListener
	public void statusListener(HashMap<String,String> result)
	{
		 Map.Entry<String,String> entry = result.entrySet().iterator().next();
		 String userId=entry.getKey();
		 String connId=entry.getValue();
		 
	}
	
	
	
	
	
	

}
