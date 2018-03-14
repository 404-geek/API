package com.aptus.blackbox.event;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.aptus.blackbox.Service.ApplicationCredentials;
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

		System.out.println("EndpointsTaskScheduler start");
		ConnectionsTaskScheduler connectionsTaskScheduler=Context.getBean(ConnectionsTaskScheduler.class);
		connectionsTaskScheduler.setConnectionsTaskScheduler(scheduleEventData.getConnId(),scheduleEventData.getUserId());
		if(scheduleEventData.getScheduled().equalsIgnoreCase("true")){
			long period = Integer.parseInt(scheduleEventData.getPeriod())*1000;
			threadPoolTaskScheduler.scheduleAtFixedRate(connectionsTaskScheduler,period);
			}
		else{
			threadPoolTaskScheduler.schedule(connectionsTaskScheduler,new Date());
		}
		
		

		
	}

	
	
	

}
