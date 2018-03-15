package com.aptus.blackbox.threading;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
@Component
@Scope("prototype")
public class ConnectionsTaskScheduler implements Runnable {

	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	@Autowired
	private ApplicationContext Context;
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private String connectionId,userId;
	private SchedulingObjects scheduleObjectInfo;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsTaskScheduler.class);

	public void setConnectionsTaskScheduler(String connectionId,String userId) {		
		this.connectionId = connectionId;
		this.userId = userId;
		this.scheduleObjectInfo = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
	}
	public void setOut(Status status) {
		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setStatus(status.getStatus());
		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setMessage(status.getMessage());
		applicationEventPublisher.publishEvent(Thread.currentThread());
	}
	@Override
	public void run() {
        ResponseEntity<String> ret = null;
        HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        try {
        	SrcObject obj = scheduleObjectInfo.getSrcObj();
            if (obj.getRefresh().equals("YES")) {
                ret = Utilities.token(obj.getRefreshToken(),scheduleObjectInfo.getSrcToken(),Thread.currentThread().getName()+"THREAD SCHEDULER RUN");
                if (!ret.getStatusCode().is2xxSuccessful()) {	
    				setOut(new Status("51","Re-authorize"));
    				return;

                } else {
                	
                	//next piece of code is ffetchEndpointsData or saveValues 
                	try {
                		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getSrcToken().putAll(new Gson().fromJson(ret.getBody(), HashMap.class));
        			} catch (Exception e) {
        				for (String s : ret.getBody().toString().split("&")) {
        					System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER RUN"+s);
        					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getSrcToken().put(s.split("=")[0], s.split("=")[1]);
        				}
        			}
            		this.scheduleObjectInfo = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
        			System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER RUN"+"token : " + scheduleObjectInfo.getSrcToken().keySet() + ":" + scheduleObjectInfo.getSrcToken().values());
                    setOut(validateData(obj.getValidateCredentials(), obj.getEndPoints()));
                    return ;
                }
            } else {
                ret = Utilities.token(obj.getValidateCredentials(),scheduleObjectInfo.getSrcToken(),Thread.currentThread().getName()+"THREAD SCHEDULER RUN");
                if (!ret.getStatusCode().is2xxSuccessful()) {                	
                	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setSrcValid(false);
                	setOut(new Status("51","Re-authorize"));
    				return;
                } else {
                	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setSrcValid(true);                    
                    setOut(fetchEndpointsData(obj.getEndPoints()));
                    return ;
                }
            }
        
        }
        catch (Exception e) {
        	System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER RUN");
        	e.printStackTrace();
            System.out.println(e);
        }
    	setOut(new Status("34","Error"));
        return ;
	}

    private Status validateData(UrlObject validateUrl, List<UrlObject> endPoints) {
        ResponseEntity<String> ret = null;
        HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        try {
            ret = Utilities.token(validateUrl,scheduleObjectInfo.getSrcToken(),Thread.currentThread().getName()+"THREAD SCHEDULER VALIDATEDATA");
            if (!ret.getStatusCode().is2xxSuccessful()) {   
				return  new Status("55","Contact Support");
				
            } else {
                return fetchEndpointsData(endPoints);
            }

        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER VALIDATEDATA"+"source.validatedata");
            e.printStackTrace();
        }
		return new Status("34","Error");
    }

    private Status fetchEndpointsData(List<UrlObject> endpoints)
    {
    	HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
    		try {
    			Gson gson=new Gson();
    			RestTemplate restTemplate = new RestTemplate();
    			
    			for(UrlObject object:endpoints) {
    				if(scheduleObjectInfo.getEndPointStatus().containsKey(object.getLabel())) {
    					System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA Starting executor");
        				EndpointsTaskExecutor endpointsTaskExecutor=Context.getBean(EndpointsTaskExecutor.class);
        				endpointsTaskExecutor.setEndpointsTaskExecutor(object, connectionId, userId,Thread.currentThread());
        				//Context.getAutowireCapableBeanFactory().autowireBean(endpointsTaskExecutor);
        				threadPoolTaskExecutor.execute(endpointsTaskExecutor);

    				}
    			}
    			return new Status("31","Success");
    		} catch (Exception e) {
    			System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA");
    			e.printStackTrace();
    			System.out.println(e+"token");
        		
    		}    		
    		return new Status("34","Error");
    }
	
	
	
}
