package com.aptus.blackbox.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.aptus.blackbox.Service.ApplicationCredentials;

@Component
public class DataListeners {
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	
	@Autowired
	private ApplicationContext Context;
	
	private String userId;
	private ScheduleObj scheduleObj;

	

	public ScheduleObj getScheduleObj() {
		return scheduleObj;
	}

	public void setScheduleObj(ScheduleObj scheduleObj) {
		this.scheduleObj = scheduleObj;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@EventListener
	public void onApplicationEventewfe(String eventData) {
		System.out.println("hfhcjrsxykjcorzti hezxgwn,rgeth"+eventData);
	}
	@EventListener
	public void onApplicationEvent(SpringEventData eventData) {

		System.out.println("EndpointsTaskExecuter start");
		
		
//		threadPoolTaskExecutor.execute(new EndpointsTaskExecuter());
		threadPoolTaskExecutor.execute(Context.getBean(EndpointsTaskExecuter.class));
		
		
		System.out.println(applicationCredentials.getApplicationCred().keySet());
		System.out.println(applicationCredentials.getApplicationCred().values());
		applicationCredentials.setApplicationCred("sava", null);
		System.out.println(applicationCredentials.getApplicationCred().keySet());
		
	}

	
	
	

}
