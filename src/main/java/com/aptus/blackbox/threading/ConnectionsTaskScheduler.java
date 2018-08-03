package com.aptus.blackbox.threading;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.event.Metering;
import com.aptus.blackbox.event.PostExecutorComplete;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.Status;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
@Component
@Scope("prototype")
public class ConnectionsTaskScheduler extends RESTFetch implements Runnable {

	
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
		if(!status.getStatus().equals("31")) {
			applicationEventPublisher.publishEvent(new InterruptThread(scheduleObjectInfo.getThread(),false, userId, connectionId));
		}
			
	}
	@Override
	public void run() {
        ResponseEntity<String> ret = null;
        try {
        	//Set scheduling and endpoint status to running(31)
        	applicationCredentials.getApplicationCred().get(userId).
    		getSchedulingObjects().get(connectionId).setMessage("Running");    		
    		applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
    				get(connectionId).setStatus("31");   	
    		for(String endpt:applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).getEndPointStatus().keySet())
    		{
    			for(String end:applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
    	    			get(connectionId).getEndPointStatus().get(endpt).keySet()) {
    				
    				applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().
        			get(connectionId).getEndPointStatus().get(endpt).put(end, new Status("31","Running"));
    			}    			
    		}
    		//publish status
    		applicationEventPublisher.publishEvent(new PostExecutorComplete(userId,connectionId));
        	SourceConfig obj = scheduleObjectInfo.getSrcObj();
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
            		scheduleObjectInfo = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
        			System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER RUN"+"token : " + scheduleObjectInfo.getSrcToken().keySet() + ":" + scheduleObjectInfo.getSrcToken().values());
        			SchedulingObjects schedulingObjects = applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId);
        			applicationEventPublisher.publishEvent(new PushCredentials(schedulingObjects.getSrcObj(), schedulingObjects.getDestObj(), schedulingObjects.getSrcToken(), 
        					schedulingObjects.getDestToken(), schedulingObjects.getSrcName(), schedulingObjects.getDestName(), userId));
        			setOut(validateData(obj.getValidateCredentials(), obj.getDataEndPoints()));
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
                    setOut(fetchEndpointsData(obj.getDataEndPoints()));
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
    		try {    			
    			Metering metring = new Metering();
    			metring.setConnId(connectionId);
    			metring.setTime(new Date()+"");
    			metring.setType("Export");
    			metring.setUserId(userId);
    			applicationCredentials.getApplicationCred().get(userId).getSchedulingObjects().get(connectionId).setMetering(metring);
    			Map<String,List<UrlObject>> endp = new HashMap<>();
    			for(UrlObject object:endpoints) {
    				if(endp.containsKey(object.getCatagory())) {
    					endp.get(object.getCatagory()).add(object);
    				}
    				else {
    					List<UrlObject> lst = new ArrayList<>();
    					lst.add(object);
        				endp.put(object.getCatagory(), lst);
    				}
    			}
    			Set<String> others = new HashSet<>();
    			
    			for(Entry<String, Map<String, Status>> catagory:scheduleObjectInfo.getEndPointStatus().entrySet()) {
    				//for(Entry<String,Status> end:catagory.getValue().entrySet()) {
    				Map<String,Status> end = catagory.getValue();
    					if(catagory.getKey().equalsIgnoreCase("others")) {  
    						others = scheduleObjectInfo.getEndPointStatus().get("others".toLowerCase()).keySet();
    						System.out.println("\t"+others);
    						System.out.println("\t"+endp.get(catagory.getKey()));
    						for(UrlObject endpnt:endp.get(catagory.getKey())) {
    							if(end.containsKey(endpnt.getLabel())) {
    								System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA Starting executor1");
    	            				EndpointsTaskExecutor endpointsTaskExecutor=Context.getBean(EndpointsTaskExecutor.class);
    	            				endpointsTaskExecutor.setEndpointsTaskExecutor(null,endpnt, connectionId, userId,Thread.currentThread(),catagory.getKey(),true);
    	            				//Context.getAutowireCapableBeanFactory().autowireBean(endpointsTaskExecutor);
    	            				threadPoolTaskExecutor.execute(endpointsTaskExecutor);
    							}
    						}
    					}
    					else {
    						UrlObject endpnt = endp.get(catagory.getKey()).get(0);
    						for(String endpntName:end.keySet()) {
    							endpnt.setLabel(endpntName);
    							Map<String,String> ne = new HashMap<>();
    							ne.put(catagory.getKey(), endpntName);
    							endpnt.setUrl(Utilities.url(endpnt.getUrl(), ne));
    							
    							System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA Starting executor2");
	            				EndpointsTaskExecutor endpointsTaskExecutor=Context.getBean(EndpointsTaskExecutor.class);
	            				endpointsTaskExecutor.setEndpointsTaskExecutor(null,endpnt, connectionId, userId,Thread.currentThread(),catagory.getKey(),true);
	            				//Context.getAutowireCapableBeanFactory().autowireBean(endpointsTaskExecutor);
	            				threadPoolTaskExecutor.execute(endpointsTaskExecutor);
    						}
    					}
    			}
    			
    			    			
    			List<String> infoendpnts = new ArrayList<>();
    			for(UrlObject endpnt:scheduleObjectInfo.getSrcObj().getInfoEndpoints()) {
    				if(others.contains(endpnt.getLabel())) {
    					infoendpnts.add(endpnt.getLabel());
    				}
    			}
    			
    			if(!infoendpnts.isEmpty()) {
    				System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA Starting executor3");
        			System.out.println(Thread.currentThread().getName()+"\t"+infoendpnts);
    				EndpointsTaskExecutor endpointsTaskExecutor=Context.getBean(EndpointsTaskExecutor.class);
    				
    				endpointsTaskExecutor.setEndpointsTaskExecutor(infoendpnts,null, connectionId, userId,Thread.currentThread(),"others",false);
    				
    				threadPoolTaskExecutor.execute(endpointsTaskExecutor);
    			}   
    			//scheduleObjectInfo.getThread().cancel(false);
    			return new Status("31","Success");
    		} catch (Exception e) {
    			System.out.println(Thread.currentThread().getName()+"THREAD SCHEDULER FETCHENDPOINTSDATA");
    			e.printStackTrace();
    			System.out.println(e+"token");
        		
    		}    		
    		return new Status("34","Error");
    }	
	
	
}
