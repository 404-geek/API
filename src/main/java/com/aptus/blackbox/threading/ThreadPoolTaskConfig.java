package com.aptus.blackbox.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;



@Configuration
@ComponentScan(basePackages = "com.aptus.blackbox")
public class ThreadPoolTaskConfig {

private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolTaskConfig.class);

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {	    	
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();
        System.out.println("threadpoolexec");
        LOGGER.info("ThreadPoolTaskExecutorConfig Configured");
        return executor;
    }
    
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler
          = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix(
          "ThreadPoolTaskScheduler");
        System.out.println("threadpoolschedule");
        LOGGER.info("ThreadPoolTaskSchedulerConfig Configured");
        return threadPoolTaskScheduler;
    }
    
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster 
          = new SimpleApplicationEventMulticaster();
         
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        
        System.out.println("ApplicationEventMulticaster");
        return eventMulticaster;
    }
    

}
