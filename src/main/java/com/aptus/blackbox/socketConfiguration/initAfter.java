package com.aptus.blackbox.socketConfiguration;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.aptus.blackbox.threading.ResourceUsageScheduler;

@Configuration
public class initAfter{
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private ApplicationContext Context;
	
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	@Primary
	@Bean
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor(); // Or use another one of your liking
	}
	
	@Bean
	public CommandLineRunner schedulingRunner(TaskExecutor executor) {
	    return new CommandLineRunner() {
	        public void run(String... args) throws Exception {
	           /* executor.execute(new SimularProfesor());*/
	        	System.out.println("Command Line runer started");
	        	ResourceUsageScheduler resourceusagescheduler = Context.getBean(ResourceUsageScheduler.class);
	    		long period = 5000;
	    		ScheduledFuture<?> future;
	    		future= threadPoolTaskScheduler.scheduleAtFixedRate(resourceusagescheduler, period);

	        }
	    };
	}
	
	
	/*@PostConstruct
	void usage() {
		applicationEventPublisher.publishEvent(new ResourceUsageEvent("chandan "));
		System.out.println("************ THIS IS INIT AFTER");
	}
	*/
	/*@EventListener(ApplicationReadyEvent.class)
	public void resourceUsage(ResourceUsageEvent resourceUsageEvent) {
		System.out.println("Received ResourceUsage scheduler: "+resourceUsageEvent.getData());
		System.out.println("Resource Usage Listener started---ResourceUageScheduler started");
		ResourceUsageScheduler resourceusagescheduler = Context.getBean(ResourceUsageScheduler.class);
		long period = 5000;
		ScheduledFuture<?> future;
		future= threadPoolTaskScheduler.schedule(resourceusagescheduler, 
				new Date(ZonedDateTime.now().toInstant().toEpochMilli()+period));

}*/

	/*@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		// TODO Auto-generated method stub
		ResourceUsageEvent resourceUsageEvent = ResourceUsageEvent();
		//System.out.println("Received ResourceUsage scheduler: "+resourceUsageEvent.getData());
		System.out.println("Resource Usage Listener started---ResourceUageScheduler started");
		ResourceUsageScheduler resourceusagescheduler = Context.getBean(ResourceUsageScheduler.class);
		long period = 5000;
		System.out.println("Future thread about to be called");

		ScheduledFuture<?> future;
//		future= threadPoolTaskScheduler.schedule(resourceusagescheduler, 
//				new Date(ZonedDateTime.now().toInstant().toEpochMilli()+period));

		
	}
*/}
