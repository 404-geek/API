package com.aptus.blackbox.threading;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.JsonObject;
import com.nimbusds.oauth2.sdk.Response;

@RestController
public class ConnectionsTaskScheduler {

	@Autowired
	private Credentials credentials;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	private @Autowired AutowireCapableBeanFactory beanFactory;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionsTaskScheduler.class);

	@RequestMapping("/x")
	public boolean checkFunc(HttpSession session)
	{
		try {			
			if(Utilities.isSessionValid(session,credentials)) {				
			
		SchedulingObjects schedulingObject=new SchedulingObjects();
		schedulingObject.setDestObj(credentials.getCurrDestObj());
		schedulingObject.setDestToken(credentials.getCurrDestToken());
		credentials.setSchedulingObjects( schedulingObject,
				credentials.getCurrConnId().getConnectionId());		
		
		EndpointsTaskExecuter endpointsTaskExecuter=
				new EndpointsTaskExecuter(credentials.getCurrSrcObj().getEndPoints().get(0),
						"view", credentials.getCurrConnId().getConnectionId());
		beanFactory.autowireBean(endpointsTaskExecuter);
		
		taskExecutor.execute(endpointsTaskExecuter);
		LOGGER.info("Output",endpointsTaskExecuter.getOut().getBody());
		return true;
			}
		else {
			System.out.println("Session expired!");
			JsonObject respBody = new JsonObject();
			respBody.addProperty("message", "Sorry! Your session has expired");
			respBody.addProperty("status", "33");
			return false;
		}
	}
		catch (Exception e) {
		e.printStackTrace();
		System.out.println("source.source");
	}
		return false;
	}
	
	
	
}
