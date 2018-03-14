package com.aptus.blackbox.threading;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
@Component
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
	
	private String connectionId,userId;
	private JsonObject out;
	private SchedulingObjects scheduleObjectInfo;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsTaskScheduler.class);

	public void setConnectionsTaskScheduler(String connectionId,String userId) {		
		this.connectionId = connectionId;
		this.userId = userId;
		this.scheduleObjectInfo = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
	}
	public JsonObject getOut() {
		return out;
	}
	public void setOut(JsonObject out) {
		this.out = out;
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
                ret = Utilities.token(obj.getRefreshToken(),scheduleObjectInfo.getSrcToken());
                if (!ret.getStatusCode().is2xxSuccessful()) {	                	
                    JsonObject respBody = new JsonObject();
        			respBody.addProperty("message", "Re-authorize");
    				respBody.addProperty("status", "51");
    				setOut(respBody);
    				//set applicationcredentials.endpointStatus
    				return;

                } else {
                	
                	//next piece of code is ffetchEndpointsDataor saveValues 
                	try {
                		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getSrcToken().putAll(new Gson().fromJson(ret.getBody(), HashMap.class));
        			} catch (Exception e) {
        				for (String s : ret.getBody().toString().split("&")) {
        					System.out.println(s);
        					applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getSrcToken().put(s.split("=")[0], s.split("=")[1]);
        				}
        			}
            		this.scheduleObjectInfo = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
        			System.out.println("token : " + scheduleObjectInfo.getSrcToken().keySet() + ":" + scheduleObjectInfo.getSrcToken().values());
                    setOut(validateData(obj.getValidateCredentials(), obj.getEndPoints()));
                    return ;
                }
            } else {
                ret = Utilities.token(obj.getValidateCredentials(),scheduleObjectInfo.getSrcToken());
                if (!ret.getStatusCode().is2xxSuccessful()) {
                	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setSrcValid(false);
                    JsonObject respBody = new JsonObject();
        			respBody.addProperty("message", "Re-authorize");
    				respBody.addProperty("status", "51");
    				setOut(respBody);
    				return ;

                } else {
                	applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setSrcValid(true);                    
                	//ret = Utilities.token(endPoints.get(0),credentials.getSrcToken());
                    setOut(fetchEndpointsData(obj.getEndPoints()));
                    return ;
                }
            }
        
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e + "home.data");
        }
		JsonObject respBody = new JsonObject();
		respBody.addProperty("status", "55");
		respBody.addProperty("data", "Error");
		setOut(respBody);
        return ;
	}

    private JsonObject validateData(UrlObject validateUrl, List<UrlObject> endPoints) {
        ResponseEntity<String> ret = null;
        HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
        try {
            ret = Utilities.token(validateUrl,scheduleObjectInfo.getSrcToken());
            if (!ret.getStatusCode().is2xxSuccessful()) {            	
                JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Contact Support");
				respBody.addProperty("status", "52");
				return  respBody;
				
            } else {
                return fetchEndpointsData(endPoints);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("source.validatedata");
        }
		JsonObject respBody = new JsonObject();
		respBody.addProperty("status", "55");
		respBody.addProperty("data", "Error");
		return respBody;
    }

    private JsonObject fetchEndpointsData(List<UrlObject> endpoints)
    {
    	HttpHeaders header = new HttpHeaders();
		header.add("Cache-Control", "no-cache");
		header.add("access-control-allow-origin", rootUrl);
        header.add("access-control-allow-credentials", "true");
    		try {
    			Gson gson=new Gson();
    			RestTemplate restTemplate = new RestTemplate();
    			
    			for(UrlObject object:endpoints) {
    				EndpointsTaskExecutor endpointsTaskExecutor=Context.getBean(EndpointsTaskExecutor.class);
    				endpointsTaskExecutor.setEndpointsTaskExecutor(object, connectionId, userId);
    				threadPoolTaskExecutor.execute(endpointsTaskExecutor);
    			}    			

    		} catch (Exception e) {
    			e.printStackTrace();
    			System.out.println(e+"token");
    		}
    		JsonObject respBody = new JsonObject();
    		respBody.addProperty("status", "55");
    		respBody.addProperty("data", "Error");
    		return respBody;
    }
	
	
	
}
