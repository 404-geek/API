package com.aptus.blackbox.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.SubscriptionService;
import com.aptus.blackbox.models.SubscribedConnectors;
import com.aptus.blackbox.models.UserAppSubscription;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class MarketingController {
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private Credentials credentials;
	
	@Autowired
	private SubscriptionService subscriptionService;
	
	
	public MarketingController() {
		System.out.println("MarkettingController Constructor");
		
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/marketing/update/{type}")
	private ResponseEntity<String> addConnectorSubscriptionData(@PathVariable String type,@RequestParam String _id,@RequestBody String data,HttpSession session){
		HttpHeaders headers = new HttpHeaders();
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				boolean result = false;
				JsonObject respBody = new JsonObject();
				
				
				switch(type) {
				
				case "subscribedConnectors":{
					SubscribedConnectors subscribedConnectors = new Gson().fromJson(data,SubscribedConnectors.class);
					result=subscriptionService.addsubscribedConnectors(_id, subscribedConnectors);
					break;
				}
				case "appSubscription":{
					UserAppSubscription userAppSubscription = new Gson().fromJson(data,UserAppSubscription.class);
					result=subscriptionService.addAappSubscription(_id, userAppSubscription);
					break;
				}
				default:{
					// log invaid
				}
					
				}
				
				if(result) {
					respBody.addProperty("message",Constants.SUCCESS_MSG);
					respBody.addProperty("status", Constants.SUCCESS_CODE);
				}
				else {
					respBody.addProperty("message",Constants.FAILED_MSG);
					respBody.addProperty("status", Constants.FAILED_CODE);
				}
				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());	
			}
				
			
		 else {
			session.invalidate();
			System.out.println("Session expired!");
			JsonObject respBody = new JsonObject();
			respBody.addProperty("message", "Sorry! Your session has expired");
			respBody.addProperty("status", "33");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
			
		}catch(Exception e){}
		
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	
}

