package com.aptus.blackbox.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.aptus.blackbox.Service.Credentials;

@Configuration
public class ThreadPoolTaskExecutorConfig {

private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolTaskExecutorConfig.class);

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {	    	
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();
        LOGGER.info("ThreadPoolTaskExecutorConfig Configured");
        return executor;
    }
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler
          = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        System.out.println("thread");
        return threadPoolTaskScheduler;
    }
}
