package com.aptus.blackbox.socketConfiguration;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.aptus.blackbox.event.ResourceUsageEvent;
import com.aptus.blackbox.threading.ResourceUsageScheduler;

@Configuration
public class initAfter {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private ApplicationContext Context;
	
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	@PostConstruct
	void usage() {
		applicationEventPublisher.publishEvent(new ResourceUsageEvent("chandan "));
		System.out.println("************ THIS IS INIT AFTER");
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void resourceUsage(ResourceUsageEvent resourceUsageEvent) {
		System.out.println("Received ResourceUsage scheduler: "+resourceUsageEvent.getData());
		System.out.println("Resource Usage Listener started---ResourceUageScheduler started");
		ResourceUsageScheduler resourceusagescheduler = Context.getBean(ResourceUsageScheduler.class);
		long period = 5000;
		ScheduledFuture<?> future;
		future= threadPoolTaskScheduler.schedule(resourceusagescheduler, 
				new Date(ZonedDateTime.now().toInstant().toEpochMilli()+period));

}
}
